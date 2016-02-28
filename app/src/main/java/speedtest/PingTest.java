package speedtest;

import android.util.Log;

import com.netvisiontel.speedtest.Interface.IPingListener;
import com.netvisiontel.speedtest.object.PingData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngodi on 2/19/2016.
 */
public class PingTest {
    private static final String TAG = "PingTest" ;
    private List<IPingListener> pingTestListenerList = new ArrayList<IPingListener>();

    public void addPingTestListener(IPingListener listener) {
        pingTestListenerList.add(listener);
    }

    public void ping(String host) {
        Runtime runtime = Runtime.getRuntime();
        Log.d(TAG, "giang dbg ping host: " + host);
        int dem = 0;
        try {
            for (int i = 0; i < pingTestListenerList.size(); i++) {
                pingTestListenerList.get(i).onPingProgress(dem);
            }

            Process ipProcess = runtime.exec("ping -c 10 " + host);
            InputStream stdout = ipProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            float[] afTime = new float[10];
            String sLoss = "";
            while ((line = reader.readLine ()) != null) {
                if(line.contains("icmp_seq")) {
                    PingData data = new PingData(line);
                    afTime[dem] = data.getTime();
                    dem++;
                    Log.d(TAG, "giang dbg ping output: " + line);
                    for (int i = 0; i < pingTestListenerList.size(); i++) {
                        pingTestListenerList.get(i).onPingProgress(data.getIcmp_seq()*10);
                    }
                } else if(line.contains("packet loss")) {
                    sLoss = PingData.getPacketLoss(line);
                    Log.d(TAG, "giang dbg ping sLoss: " + sLoss);
                }
            }
            float time = 0;
            for(int i = 0; i < 10; i++) {
                time += afTime[i];
            }
            Log.d(TAG, "giang dbg ping time total: " + time);
            time = time/10;
            for (int i = 0; i < pingTestListenerList.size(); i++) {
                pingTestListenerList.get(i).onPingReceived(time, sLoss);
            }
            int     exitValue = ipProcess.waitFor();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pingUrl(String host) {
        Runtime runtime = Runtime.getRuntime();
        Log.d(TAG, "giang dbg ping url: " + host);
        long[] afTime = new long[10];
        int loss = 0;
        try {
            for (int i = 0; i < pingTestListenerList.size(); i++) {
                pingTestListenerList.get(i).onPingProgress(0);
            }
            for(int i = 1; i <= 10; i++) {

                URL url = new URL("http://" + host);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(1000 * 2); // mTimeout is in seconds
                long startTime = System.currentTimeMillis();
                urlConn.connect();
                long endTime = System.currentTimeMillis();
                if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, i + " Ping Time (ms) : " + (endTime - startTime));
                    afTime[i] = endTime - startTime;
                    for (int j = 0; j < pingTestListenerList.size(); j++) {
                        pingTestListenerList.get(i).onPingProgress(i*10);
                    }
                } else {
                    loss++;
                }
            }
            float time = 0;
            float fLoss;
            for(int i = 0; i < 10; i++) {
                time += afTime[i];
            }
            Log.d(TAG, "giang dbg ping time total: " + time);
            time = time/10;
            fLoss = loss*100/10;
            for (int i = 0; i < pingTestListenerList.size(); i++) {
                pingTestListenerList.get(i).onPingReceived(time, String.valueOf(fLoss));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
