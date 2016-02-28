package com.netvisiontel.speedtest.object;

import android.util.Log;

/**
 * Created by ngodi on 2/19/2016.
 */
public class PingData {
    private static final String TAG = "PingData";
    public int ttl;
    public float time;
    public int icmp_seq;

    public PingData(String in) {
        String[] tmp = in.split(" ");
        for(String s : tmp) {
//            Log.d(TAG, "giang dbg: " + s);
            if(s.contains("icmp_seq")) {
                this.icmp_seq = Integer.valueOf(s.split("=")[1]);
            } else if(s.contains("ttl")) {
                this.ttl = Integer.valueOf(s.split("=")[1]);
            } else if(s.contains("time")) {
                this.time = Float.valueOf(s.split("=")[1]);
            }
        }

    }

    public int getIcmp_seq() {
        return icmp_seq;
    }

    public float getTime() {
        return time;
    }

    public static String getPacketLoss(String in) {
       String[] tmp = in.split(", ");
        for(String s : tmp) {
            if(s.contains("packet loss")) {
                return s.split(" ")[0];
            }
        }
        return null;
    }
}
