package com.netvisiontel.speedtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netvisiontel.speedtest.object.SpeedData;
import com.netvisiontel.speedtest.object.UpDownObject;
import com.netvisiontel.speedtest.util.Config;
import com.netvisiontel.speedtest.util.Network;
import com.netvisiontel.speedtest.wiget.SpeedView;

import speedtest.Download;
import com.netvisiontel.speedtest.Interface.IDownloadListener;
import com.netvisiontel.speedtest.Interface.IUploadListener;
import speedtest.Upload;

/**
 * Created by ngodi on 2/24/2016.
 */
public class SpeedActivity extends Activity {
    private static final String TAG = "SpeedActivity";
    Context context;
    ImageButton ibSetting;
    RelativeLayout rlSpeed;
    TextView tvMaxResult, tvAvgResult;
    ProgressBar pbStatus;
    ImageButton ibStartSpeed;
    AsyncTask<Void, Void, String> atSpeedTest;
    SpeedView svSpeedDisplay;
    SharedPreferences prefs;
    public boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedtest);
        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ibSetting = (ImageButton) findViewById(R.id.ibSetting);
        rlSpeed = (RelativeLayout) findViewById(R.id.rlSpeed);
        tvMaxResult = (TextView) findViewById(R.id.tvMaxResult);
        tvAvgResult = (TextView) findViewById(R.id.tvAvgResult);
        svSpeedDisplay = (SpeedView) findViewById(R.id.svSpeedDisplay);
        pbStatus = (ProgressBar) findViewById(R.id.pbStatus);
        ibStartSpeed = (ImageButton) findViewById(R.id.ibStartSpeed);

        ibSetting.setOnClickListener(settingListener);
        ibStartSpeed.setOnClickListener(startListener);
        String ip = prefs.getString(Config.PREF_KEY_SERVER_HOST, null);
        int port = prefs.getInt(Config.PREF_KEY_SERVER_PORT, 0);
        if((ip == null) || (port == 0))
            SettingServer(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        loManager.removeUpdates(gpslistener);

    }



    final Handler hSpeedCircle = new Handler() {
        public void handleMessage(Message msg) {
            float val = (float) msg.obj;
            svSpeedDisplay.setValue(val);
        }
    };

    final Handler hDownload = new Handler() {
        public void handleMessage(Message msg) {
            pbStatus.setVisibility(View.GONE);
            rlSpeed.setVisibility(View.VISIBLE);
            UpDownObject object = (UpDownObject) msg.obj;
            tvMaxResult.setText(String.format("%.2f", object.getMax()));
            tvAvgResult.setText(String.format("%.2f", object.getAvg()));
            svSpeedDisplay.setValue(object.getMax());
        }
    };

    final Handler hUpload = new Handler() {
        public void handleMessage(Message msg) {
            UpDownObject object = (UpDownObject) msg.obj;
            tvMaxResult.setText(String.format("%.2f", object.getMax()));
            tvAvgResult.setText(String.format("%.2f", object.getAvg()));
            svSpeedDisplay.setValue(object.getMax());
        }
    };

    final Handler hStatus = new Handler() {
        public void handleMessage(Message msg) {
            SpeedData data = (SpeedData)msg.obj;
            pbStatus.setVisibility(View.VISIBLE);
        }
    };

    final Handler hButton = new Handler() {
        public void handleMessage(Message msg) {
            SpeedData data = (SpeedData)msg.obj;
            pbStatus.setVisibility(View.VISIBLE);
            ibStartSpeed.setVisibility(View.VISIBLE);
        }
    };

    public OnClickListener settingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SettingServer(context);
        }
    };
    public OnClickListener startListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            rlSpeed.setVisibility(View.GONE);
            svSpeedDisplay.setValue(0);
            if(!isRunning) {
                atSpeedTest = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        isRunning = true;
//                    Log.d(TAG, "uri download: " + sdServer.getUriDownload());
//                    Download down = new Download(sdServer.getHost(), 80, sdServer.getUriDownload(),  Config.DOWNLOAD_FILE);
                        String ip = prefs.getString(Config.PREF_KEY_SERVER_HOST, null);
                        int port = prefs.getInt(Config.PREF_KEY_SERVER_PORT, 0);
                        Log.d(TAG, "ip: " + ip + ", port: " + port);
                        Download down = new Download(ip, port, "/speedtest/", Config.DOWNLOAD_FILE);
                        down.addDownloadTestListener(new IDownloadListener() {
                            @Override
                            public void onDownloadPacketsReceived(float transferRateBitPerSeconds, float download_avg) {
                                Log.d(TAG, "Download [ OK ]");
                                Log.d(TAG, "download transfer rate  : " + transferRateBitPerSeconds + " Mbit/second");
                                Log.d(TAG, "##################################################################");

                                UpDownObject data = new UpDownObject(transferRateBitPerSeconds, download_avg);
                                Message msg = new Message();
                                msg.obj = data;
                                hDownload.sendMessage(msg);
                            }


                            @Override
                            public void onDownloadError(int errorCode, String message) {

                            }

                            @Override
                            public void onDownloadProgress(int percent) {
//                            Log.d(TAG, "Download  percent: " + percent);
                                if (percent == 0) {
                                    Message msg = new Message();
                                    hStatus.sendMessage(msg);
                                }
                                pbStatus.setProgress(percent);
                            }

                            @Override
                            public void onDownloadUpdate(float speed) {
                                Log.d(TAG, "onDownloadUpdate speed: " + speed);
                                Message msg = new Message();
                                msg.obj = speed;
                                hSpeedCircle.sendMessage(msg);
                            }
                        });
                        down.Download_Run();
                        Upload up = new Upload(ip, 80, "/", Config.UPLOAD_SIZE);
                        up.addUploadTestListener(new IUploadListener() {
                            @Override
                            public void onUploadPacketsReceived(float transferRateBitPerSeconds, float upload_avg) {
                                Log.d(TAG, "========= Upload [ OK ]   =============");
                                Log.d(TAG, "upload transfer rate  : " + transferRateBitPerSeconds + " Mbit/second");
                                Log.d(TAG, "##################################################################");
                                UpDownObject data = new UpDownObject(transferRateBitPerSeconds, upload_avg);
                                Message msg = new Message();
                                msg.obj = data;
                                hUpload.sendMessage(msg);
                            }

                            @Override
                            public void onUploadError(int errorCode, String message) {

                            }

                            @Override
                            public void onUploadProgress(int percent) {
//                            Log.d(TAG, "Upload  percent: " + percent);
                            }

                            @Override
                            public void onUploadUpdate(float speed) {
                                Log.d(TAG, "onDownloadUpdate speed: " + speed);
                                Message msg = new Message();
                                msg.obj = speed;
                                hSpeedCircle.sendMessage(msg);
                            }
                        });
//                    up.Upload_Run();
                        isRunning = false;
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String result) {

                    }

                };
                atSpeedTest.execute(null, null, null);
            } else
                Toast.makeText(context, "upload or download progress is running", Toast.LENGTH_LONG).show();
        }
    };

    public void SettingServer(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.settingserver);
        dialog.setTitle("Setting Server");

        final EditText etServerIp = (EditText)dialog.findViewById(R.id.etServerIp);
        final EditText etServerPort = (EditText)dialog.findViewById(R.id.etServerPort);
        String ip = prefs.getString(Config.PREF_KEY_SERVER_HOST, null);
        int port = prefs.getInt(Config.PREF_KEY_SERVER_PORT, 0);
        if(ip != null)
            etServerIp.setText(ip);
        if(port > 0)
            etServerPort.setText(String.valueOf(port));
        Button btSettingServerOk = (Button)dialog.findViewById(R.id.btSettingServerOk);
        btSettingServerOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    SharedPreferences.Editor editor = prefs.edit();
                    String ip = etServerIp.getText().toString().trim();
                    if (!Network.isIP(ip))
                        Toast.makeText(SpeedActivity.this, "Server host is not ip address", Toast.LENGTH_LONG).show();
                    int port = Integer.valueOf(etServerPort.getText().toString().trim());
                    if (Network.CheckHost(ip)) {
                        editor.putString(Config.PREF_KEY_SERVER_HOST, ip);
                        editor.putInt(Config.PREF_KEY_SERVER_PORT, port);
                        Config.strServer_Ip = ip;
                        Config.iServer_Port = port;
                        Log.d(TAG, "ip: " + Config.strServer_Ip + ", port: " + Config.iServer_Port);
                        editor.commit();
                        dialog.dismiss();
                    } else
                        Toast.makeText(SpeedActivity.this, "Can not ping to server ip", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SpeedActivity.this, "port is number", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }
}

