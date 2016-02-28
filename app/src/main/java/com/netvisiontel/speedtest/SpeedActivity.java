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
    RelativeLayout rlDownloadSpeed, rlUploadSpeed;
    TextView tvDownloadMaxResult, tvDownloadAvgResult;
    TextView tvUploadMaxResult, tvUploadAvgResult;
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
        rlDownloadSpeed = (RelativeLayout) findViewById(R.id.rlDownloadSpeed);
        rlUploadSpeed = (RelativeLayout) findViewById(R.id.rlUploadSpeed);
        tvDownloadMaxResult = (TextView) findViewById(R.id.tvDownloadMaxResult);
        tvDownloadAvgResult = (TextView) findViewById(R.id.tvDownloadAvgResult);
        tvUploadMaxResult = (TextView) findViewById(R.id.tvUploadMaxResult);
        tvUploadAvgResult = (TextView) findViewById(R.id.tvUploadAvgResult);
        svSpeedDisplay = (SpeedView) findViewById(R.id.svSpeedDisplay);
        pbStatus = (ProgressBar) findViewById(R.id.pbStatus);
        ibStartSpeed = (ImageButton) findViewById(R.id.ibStartSpeed);
        ibStartSpeed.setOnClickListener(startListener);
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
            rlDownloadSpeed.setVisibility(View.VISIBLE);
            UpDownObject object = (UpDownObject) msg.obj;
            tvDownloadMaxResult.setText(String.format("Max:     %.2fMbps", object.getMax()));
            tvDownloadAvgResult.setText(String.format("Avg:     %.2fMbps", object.getAvg()));
            svSpeedDisplay.setValue(0);
        }
    };

    final Handler hUpload = new Handler() {
        public void handleMessage(Message msg) {
            UpDownObject object = (UpDownObject) msg.obj;
            tvUploadMaxResult.setText(String.format("Max:     %.2fMbps", object.getMax()));
            tvUploadAvgResult.setText(String.format("Avg:     %.2fMbps", object.getAvg()));
            svSpeedDisplay.setValue(0);
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


    public OnClickListener startListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            tvDownloadMaxResult.setText("");
            tvDownloadAvgResult.setText("");
            rlDownloadSpeed.setVisibility(View.GONE);
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

