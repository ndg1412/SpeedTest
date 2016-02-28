package speedtest;

import android.util.Log;

import com.netvisiontel.speedtest.Interface.IUploadListener;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;

/**
 * Created by ngodi on 2/22/2016.
 */
public class Upload_Test {
    private static final String TAG = "Upload";
    String host;
    int port;
    String uri;
    int[] sizes;
    int total_size = 0;
    int finish_size = 0;
    private List<IUploadListener> uploadTestListenerList = new ArrayList<IUploadListener>();
    public void addPingTestListener(IUploadListener listener) {
        uploadTestListenerList.add(listener);
    }

    public Upload_Test(String host, int port, String uri, int[] sizes) {
        this.host = host;
        this.port = port;
        this.uri = uri;
        this.sizes = sizes;
        for(int i : sizes)
            total_size += i;
        Log.d(TAG, "total_size: " + total_size);
    }

    public String Create_Head(int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("POST %s HTTP/1.0 \r\n");
        sb.append("Host :%s \r\n");
        sb.append("Accept: **/*//*\r\n");
        sb.append("Content-Length: %s \r\n");
        sb.append("Connection: keep-alive \r\n");
        sb.append("Content-Type: application/x-www-form-urlencoded\r\n");
//		sb.append("Content-Type: multipart/form-data; charset=utf-8; boundary=\"another cool boundary\"\r\n");
        sb.append("Expect: 100-continue\r\n");
        sb.append("\r\n");

        String uploadRequest = String.format(sb.toString(), uri, host, size);
        Log.d(TAG, "uploadRequest: " + uploadRequest);
        return uploadRequest;
    }

    public void Upload_Run() {
        long timeStart = System.currentTimeMillis();
        int finish_size = 0;
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i : sizes) {
            finish_size += Do_Upload(host, port, i);
        }
        long timeEnd = System.currentTimeMillis();
        float transferRate_bps = (finish_size * 8) / ((timeEnd - timeStart) / 1000f);
        float transferRate_Bps = finish_size / ((timeEnd - timeStart) / 1000f);
        Log.d(TAG, "total_size: " + total_size);
        Log.d(TAG, "finish_size: " + finish_size);
        Log.d(TAG, "upload transfer rate  : " + transferRate_bps + " bit/second   | " + transferRate_bps / 1000
                + " Kbit/second  | " + transferRate_bps / 1000000 + " Mbit/second");

    }

    public int Do_Upload(String host, int port, int size) {
        String request = Create_Head(size);
        RandomGen random = new RandomGen(size);
        try {
            Socket socket = new Socket();
            socket.setTcpNoDelay(false);
            socket.setSoTimeout(10 * 1000);
                /* establish socket parameters */
            socket.setReuseAddress(true);

            socket.setKeepAlive(true);

            socket.connect(new InetSocketAddress(host, port));
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.getBytes());
            outputStream.flush();
            outputStream.write(random.getBuf());
            outputStream.flush();
            HttpFrame frame = new HttpFrame();

            HttpStates httpStates = frame.parseHttp(socket.getInputStream());
            System.out.println("giang dbg httpStates: " + httpStates.name());
            System.out.println("giang dbg getStatusCode: " + frame.getStatusCode());
            System.out.println("giang dbg frame.getReasonPhrase(): " + frame.getReasonPhrase());
            if (httpStates == HttpStates.HTTP_FRAME_OK) {
                Log.d(TAG, "HttpStates.HTTP_FRAME_OK");
                if (frame.getStatusCode() == 200 && frame.getReasonPhrase().toLowerCase().equals("ok")) {
                    for (int i = 0; i < uploadTestListenerList.size(); i++)

                        uploadTestListenerList.get(i).onUploadProgress((int)(100*size/total_size));
                    Log.d(TAG, "upload complete==============================>");
                    return size;
                }

            } else if (httpStates == HttpStates.HTTP_READING_ERROR) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }
}
