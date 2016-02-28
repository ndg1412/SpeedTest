package com.netvisiontel.speedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.netvisiontel.speedtest.Server.Address_Location;
import com.netvisiontel.speedtest.Server.BestServer;
import com.netvisiontel.speedtest.Server.ServerData;
import com.netvisiontel.speedtest.object.PingObject;
import com.netvisiontel.speedtest.object.SpeedData;
import com.netvisiontel.speedtest.util.Config;
import com.netvisiontel.speedtest.Server.LoadServer;
import com.netvisiontel.speedtest.util.Network;

import speedtest.Download;
import com.netvisiontel.speedtest.Interface.IDownloadListener;
import com.netvisiontel.speedtest.Interface.IPingListener;
import com.netvisiontel.speedtest.Interface.IUploadListener;
import speedtest.PingTest;
import speedtest.SpeedTestSocket;
import speedtest.Upload;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    Context context;
    ProgressBar pbDownload, pbUpload, pbLatency, pbLoss;
    TextView tvDownloadResult, tvUploadResult;
    TextView tvLatencyResult, tvLossResult, tvBestServer;
    SpeedTestSocket stsSpeedTest;
    Upload uploadtest;
    AsyncTask<Void, Void, String> atSpeedTest;
    SharedPreferences prefs;
    LocationManager loManager;
    LoadServer loadServer;
    ProgressDialog pdBestServer;
    ServerData sdServer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tvBestServer = (TextView) findViewById(R.id.tvBestServer);
        tvDownloadResult = (TextView) findViewById(R.id.tvDownloadResult);
        tvUploadResult = (TextView) findViewById(R.id.tvUploadResult);
        tvLatencyResult = (TextView) findViewById(R.id.tvLatencyResult);
        tvLossResult = (TextView) findViewById(R.id.tvLossResult);
        pbDownload = (ProgressBar) findViewById(R.id.pbDownload);
        pbUpload = (ProgressBar) findViewById(R.id.pbUpload);
        pbLatency = (ProgressBar) findViewById(R.id.pbLatency);
        pbLoss = (ProgressBar) findViewById(R.id.pbLoss);

        Log.d(TAG, "giang dbg rx byte: " + Network.getRxByte("wlan0"));
        Log.d(TAG, "3g interface name: " + Network.getLTEIfName());
        ImageButton ibStart = (ImageButton) findViewById(R.id.ibStart);
        ibStart.setOnClickListener(startListener);
        loManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        CheckSettingGps(this);
//        loManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, networklistener);
//        loManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, gpslistener);
//        Select_BestServer();
//        loadServer = new LoadServer(this);
        SettingServer(context);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        loManager.removeUpdates(gpslistener);

    }

    LocationListener networklistener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(final Location location) {
            // TODO Auto-generated method stub
            Log.d(TAG, "location lat: " + location.getLatitude() + ", lon: " + location.getLongitude());
            if(sdServer == null) {
                final String country = Address_Location.getCountry(context, location);
//                final String country = "India";
                Log.d(TAG, "country: " + country);
                if(country != null) {
                    loManager.removeUpdates(networklistener);
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            sdServer = BestServer.getBestServer(loadServer.getServerInCountry(country));
                            if (sdServer != null) {
                                Log.e(TAG, " giang dbg best server host: " + sdServer.getHost());
                                pdBestServer.dismiss();
                                Message msg = new Message();
                                Location lDist = new Location("");
                                lDist.setLatitude(sdServer.getLat());
                                lDist.setLongitude(sdServer.getLon());
                                float distance = location.distanceTo(lDist);
                                msg.obj = String.format("%s, %s, %s, %.2f Km", sdServer.getSponsor(), sdServer.getName(), sdServer.getCountry(), distance/1000);
                                hBestServer.sendMessage(msg);
                            }
                            return "";
                        }

                        @Override
                        protected void onPostExecute(String result) {

                        }

                    }.execute(null, null, null);
                }
            }
        }
    };

    LocationListener gpslistener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(final Location location) {
            // TODO Auto-generated method stub
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            Log.d(TAG, "location lat: " + lat + ", lon: " + lon);
            if(sdServer == null) {
                final String country = Address_Location.getCountry(context, location);
//                final String country = "India";
                if(country != null) {
                    loManager.removeUpdates(gpslistener);
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            sdServer = BestServer.getBestServer(loadServer.getServerInCountry(country));
                            if (sdServer != null) {
                                Log.e(TAG, " giang dbg best server host: " + sdServer.getHost());
                                pdBestServer.dismiss();
                                Message msg = new Message();
                                Location lDist = new Location("");
                                lDist.setLatitude(sdServer.getLat());
                                lDist.setLongitude(sdServer.getLon());
                                float distance = location.distanceTo(lDist);
                                msg.obj = String.format("%s, %s, %s, %.2f Km", sdServer.getSponsor(), sdServer.getName(), sdServer.getCountry(), distance/1000);
                                hBestServer.sendMessage(msg);
                            }
                            return "";
                        }

                        @Override
                        protected void onPostExecute(String result) {

                        }

                    }.execute(null, null, null);
                }
            }
        }
    };

    final Handler hBestServer = new Handler() {
        public void handleMessage(Message msg) {
            String server = (String) msg.obj;
            tvBestServer.setText(server);
        }
    };


    final Handler hDownload = new Handler() {
        public void handleMessage(Message msg) {
            SpeedData data = (SpeedData)msg.obj;
            switch(data.getMode()) {
                case 0:
                    pbDownload.setVisibility(View.VISIBLE);
                    tvDownloadResult.setVisibility(View.GONE);
                    break;
                case 1:
                    pbDownload.setVisibility(View.GONE);
                    tvDownloadResult.setVisibility(View.VISIBLE);
                    tvDownloadResult.setText(data.getSpeed());

                    break;
                default:
                    break;
            }
        }
    };

    final Handler hUpload = new Handler() {
        public void handleMessage(Message msg) {
            SpeedData data = (SpeedData)msg.obj;
            switch(data.getMode()) {
                case 0:
                    pbUpload.setVisibility(View.VISIBLE);
                    tvUploadResult.setVisibility(View.GONE);

                    break;
                case 1:
                    pbUpload.setVisibility(View.GONE);
                    tvUploadResult.setVisibility(View.VISIBLE);
                    tvUploadResult.setText(data.getSpeed());
                    break;
                default:
                    break;
            }
        }
    };

    final Handler hPing = new Handler() {
        public void handleMessage(Message msg) {
            PingObject data = (PingObject)msg.obj;
            switch(data.getMode()) {
                case 0:
                    pbLatency.setVisibility(View.VISIBLE);
                    pbLoss.setVisibility(View.VISIBLE);
                    tvLatencyResult.setVisibility(View.GONE);
                    tvLossResult.setVisibility(View.GONE);

                    break;
                case 1:
                    pbLatency.setVisibility(View.GONE);
                    pbLoss.setVisibility(View.GONE);
                    tvLatencyResult.setVisibility(View.VISIBLE);
                    tvLossResult.setVisibility(View.VISIBLE);
                    tvLatencyResult.setText(String.format("%.2f ms", data.getTime()));
                    tvLossResult.setText(data.getPacketLoss());
                    break;
                default:
                    break;
            }
        }
    };

    public View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            tvDownloadResult.setVisibility(View.GONE);
            tvUploadResult.setVisibility(View.GONE);
            tvLatencyResult.setVisibility(View.GONE);
            tvLossResult.setVisibility(View.GONE);

            pbDownload.setProgress(0);
            pbDownload.setVisibility(View.GONE);
            pbUpload.setProgress(0);
            pbUpload.setVisibility(View.GONE);
            pbLatency.setProgress(0);
            pbLatency.setVisibility(View.GONE);
            pbLoss.setProgress(0);
            pbLoss.setVisibility(View.GONE);

            atSpeedTest = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
//                    Log.d(TAG, "uri download: " + sdServer.getUriDownload());
//                    Download down = new Download(sdServer.getHost(), 80, sdServer.getUriDownload(),  Config.DOWNLOAD_FILE);
                    String ip = prefs.getString(Config.PREF_KEY_SERVER_HOST, null);
                    int port = prefs.getInt(Config.PREF_KEY_SERVER_PORT, 0);
                    Download down = new Download(ip, port, "/speedtest/",  Config.DOWNLOAD_FILE);
                    down.addDownloadTestListener(new IDownloadListener() {
                        @Override
                        public void onDownloadPacketsReceived(float transferRateBitPerSeconds, float download_avg) {
                            Log.d(TAG, "Download [ OK ]");
                            Log.d(TAG, "download transfer rate  : " + transferRateBitPerSeconds + " Mbit/second");
                            Log.d(TAG, "##################################################################");

                            SpeedData data = new SpeedData(1, String.format("max: %.2fMbps, avg: %.2fMbps", transferRateBitPerSeconds, download_avg));
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
                            pbDownload.setProgress(percent);
                            if (percent == 0) {
                                SpeedData data = new SpeedData(0);
                                Message msg = new Message();
                                msg.obj = data;
                                hDownload.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onDownloadUpdate(float speed) {

                        }
                    });
                    down.Download_Run();
//                    Upload up = new Upload(sdServer.getHost(), 80, sdServer.getUri(),  Config.UPLOAD_SIZE);
                    Upload up = new Upload(ip, port, "/",  Config.UPLOAD_SIZE);
//                    Upload up = new Upload("www.hizinitestet.com", 80, "/speedtest/upload.aspx",  Config.UPLOAD_SIZE);
                    up.addUploadTestListener(new IUploadListener() {
                        @Override
                        public void onUploadPacketsReceived(float transferRateBitPerSeconds, float upload_avg) {
                            Log.d(TAG, "========= Upload [ OK ]   =============");
                            Log.d(TAG, "upload transfer rate  : " + transferRateBitPerSeconds + " Mbit/second");
                            Log.d(TAG, "##################################################################");
                            SpeedData data = new SpeedData(1, String.format("max: %.2fMbps, avg: %.2fMbps", transferRateBitPerSeconds, upload_avg));
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
                            pbUpload.setProgress(percent);
                            if (percent == 0) {
                                SpeedData data = new SpeedData(0);
                                Message msg = new Message();
                                msg.obj = data;
                                hUpload.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onUploadUpdate(float speed) {

                        }
                    });
//                    up.Upload_Run();
//                    pingtest.ping(sdServer.getHost());

                    PingTest pingtest = new PingTest();
                    pingtest.ping(ip);
                    pingtest.addPingTestListener(new IPingListener() {
                        @Override
                        public void onPingReceived(float time, String loss) {
                            PingObject data = new PingObject(1, time, loss);
                            Message msg = new Message();
                            msg.obj = data;
                            hPing.sendMessage(msg);
                        }

                        @Override
                        public void onPingProgress(int percent) {
                            pbLatency.setProgress(percent);
                            pbLoss.setProgress(percent);
                            PingObject data = new PingObject(0);
                            Message msg = new Message();
                            msg.obj = data;
                            hPing.sendMessage(msg);
                        }

                        @Override
                        public void onPingError(int errorCode, String message) {

                        }
                    });

                    return "";
                }

                @Override
                protected void onPostExecute(String result) {

                }

            };
            atSpeedTest.execute(null, null, null);
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
                    if(!Network.isIP(ip))
                        Toast.makeText(MainActivity.this, "Server host is not ip address", Toast.LENGTH_LONG).show();
                    int port = Integer.valueOf(etServerPort.getText().toString().trim());
                    if(Network.CheckHost(ip)) {
                        editor.putString(Config.PREF_KEY_SERVER_HOST, ip);
                        editor.putInt(Config.PREF_KEY_SERVER_PORT, port);
                        Config.strServer_Ip = ip;
                        Config.iServer_Port = port;
                        Log.d(TAG, "ip: " + Config.strServer_Ip + ", port: " + Config.iServer_Port);
                        editor.commit();
                        dialog.dismiss();
                    } else
                        Toast.makeText(MainActivity.this, "Can not ping to server ip", Toast.LENGTH_LONG).show();
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "port is number", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    public void CheckSettingGps(Context context) {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = loManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){}
        try {
            network_enabled = loManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex){}
        if ( !gps_enabled && !network_enabled ){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
            dialog.setMessage("GPS not enabled. Click Ok setting location");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    dialog.dismiss();
                }
            });
            AlertDialog alert = dialog.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
        }
    }

    public void Select_BestServer() {
        pdBestServer = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        pdBestServer.setTitle("Select best server");
        pdBestServer.setMessage("loading ...");
        pdBestServer.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdBestServer.setCancelable(false);
        pdBestServer.setCanceledOnTouchOutside(false);
        pdBestServer.setIndeterminate(false);
        pdBestServer.show();
    }
}
