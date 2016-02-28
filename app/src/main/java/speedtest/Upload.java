package speedtest;

import android.util.Log;

import com.netvisiontel.speedtest.Interface.IUploadListener;
import com.netvisiontel.speedtest.util.Config;
import com.netvisiontel.speedtest.util.Network;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;


/**
 * Created by ngodi on 2/20/2016.
 */
public class Upload {
    private static final String TAG = "Upload";
    String host;
    int port;
    String uri;
    int[] sizes;
    long total_size = 0;
    long finish_size = 0;
    int total_upload = 0;
    String sLteName = null;
    Timer tiUpload = null;
    List<Float> lMax = new ArrayList<Float>();
    private IUploadListener uploadTestListenerList;
    public void addUploadTestListener(IUploadListener listener) {
        uploadTestListenerList = listener;
    }

    public Upload(String host, int port, String uri, int[] sizes) {
        this.host = host;
        this.port = port;
        this.uri = uri;
        this.sizes = sizes;
        sLteName = Network.getLTEIfName();
        for(int i : sizes)
            total_size += i;
        Log.d(TAG, "total_size: " + total_size);
    }

    public String Create_Head(int size) {
//        String uploadRequest = "POST " + uri + " HTTP/1.1\r\n" + "Host: " + host + "\r\nAccept: */*\r\nContent-Length: " + size + "\r\n\r\n";
//        Log.d(TAG, "Create_Head uploadRequest size: " + uploadRequest.length());
        StringBuilder sb = new StringBuilder();
        sb.append("POST %s HTTP/1.0 \r\n");
        sb.append("Host :%s \r\n");
        sb.append("Accept: **/*//*\r\n");
        sb.append("Content-Length: %s \r\n");
        sb.append("Connection: keep-alive \r\n");
        sb.append("Content-Type: application/x-www-form-urlencoded\r\n");
		sb.append("Content-Type: multipart/form-data; charset=utf-8; boundary=\"another cool boundary\"\r\n");
        sb.append("Expect: 100-continue\r\n");
        sb.append("\r\n");

        String uploadRequest = String.format(sb.toString(), uri, host, size);
        return uploadRequest;
    }

    public void Upload_Run() {
        uploadTestListenerList.onUploadProgress(0);

        BlockingQueue queue = new LinkedBlockingQueue(Config.NUMBER_QUEUE_THREAD);
        Producer procedure = new Producer(queue, sizes);
        Consumer consumer = new Consumer(queue, total_size);
        Thread thPro = new Thread(procedure);
        Thread thCon = new Thread(consumer);
        long timeStart = System.currentTimeMillis();
        thPro.start();
        thCon.start();

        tiUpload = new Timer();
        tiUpload.schedule(new TimerTask() {
            @Override
            public void run() {
                float upload = ((float)total_upload*8/1000000*(1000f/(Config.TIMER_SLEEP)));
                lMax.add(upload);
                total_upload = 0;
                Log.d(TAG, "speed get byte: " + upload);
            }
        }, 0, Config.TIMER_SLEEP);
        try {
            while(thPro.isAlive())
                thPro.join(100);
            while(thCon.isAlive())
                thCon.join(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long timeEnd = System.currentTimeMillis();
        long wlan_tx_end = Network.getTxByte(Config.WLAN_IF);
        long lte_tx_end = Network.getTxByte(sLteName);
        float transferRate_bps = getBandwidth();
        float transfer = (finish_size * 8) / ((timeEnd - timeStart) / 1000f)/1000000;
        Log.d(TAG, "upload transfer speed  : " + transfer + "Mbps");
        uploadTestListenerList.onUploadPacketsReceived(transferRate_bps, transfer);
        Log.d(TAG, "max upload bandwidth: " + getBandwidth());
        tiUpload.cancel();
        tiUpload = null;
    }

    public class Do_Upload extends Thread {
        int size;
        int upload_size = 0;

        public Do_Upload(int size) {
            this.size = size;

        }
        @Override
        public void run() {
            String request = Create_Head(size);
            RandomGen random = new RandomGen(size);
            byte[] buf = random.getBuf();
            Socket socket = null;
            try {
                socket = new Socket();
//                socket.setTcpNoDelay(false);
//                socket.setSoTimeout(10 * 1000);
                /* establish socket parameters */
                socket.setReuseAddress(true);

                socket.setKeepAlive(true);

                socket.connect(new InetSocketAddress(host, port));
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(request.getBytes());
                outputStream.flush();
                outputStream.write(buf);
                outputStream.flush();
//                for(int i = 0; i < buf.length; i = i + 10000) {
//                    outputStream.write(buf, i, 10000);
//                    outputStream.flush();
//                    total_upload += 10000;
//                }
                HttpFrame frame = new HttpFrame();

                HttpStates httpStates = frame.parseHttp(socket.getInputStream());
                if (httpStates == HttpStates.HTTP_FRAME_OK) {
                    if (frame.getStatusCode() == 200 && frame.getReasonPhrase().toLowerCase().equals("ok")) {
                        upload_size = size;

//                        Log.d(TAG, "upload complete==============================>");

                    }

                } else if (httpStates == HttpStates.HTTP_READING_ERROR) {

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getUploadSize() {
            return upload_size;
        }
    }

    public class Producer implements Runnable {
        BlockingQueue queue;
        int[] sizes;

        public Producer(BlockingQueue queue, int[] sizes) {
            this.queue = queue;
            this.sizes = sizes;
        }

        @Override
        public void run() {
            for(int i : sizes) {
                Do_Upload up = new Do_Upload(i);
                up.start();
                try {
                    queue.put(up);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class Consumer implements Runnable {
        BlockingQueue queue;
        long total_size;

        public Consumer(BlockingQueue queue, long total_size) {
            this.queue = queue;
            this.total_size = total_size;
        }
        @Override
        public void run() {

            while(finish_size < total_size) {
                try {
                    Do_Upload up = (Do_Upload)queue.take();
                    while (up.isAlive()) {
                        up.join(100);
                    }
                    finish_size += up.getUploadSize();
                    uploadTestListenerList.onUploadProgress((int)(100*finish_size/total_size));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public float getBandwidth() {
        float max = lMax.get(0);
        for(int i = 1; i < lMax.size(); i++) {
            if(max < lMax.get(i))
                max = lMax.get(i);
        }
        return max;
    }
}
