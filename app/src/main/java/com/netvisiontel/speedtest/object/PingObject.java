package com.netvisiontel.speedtest.object;

/**
 * Created by ngodi on 2/19/2016.
 */
public class PingObject {
    int mode = 0;
    float time;
    String packet_loss;

    public PingObject(int mode) {
        this.mode = mode;
    }

    public PingObject(int mode, float time, String loss) {
        this.mode = mode;
        this.time = time;
        this.packet_loss = loss;
    }

    public int getMode() {
        return mode;
    }

    public float getTime() {
        return time;
    }

    public String getPacketLoss() {
        return packet_loss;
    }
}
