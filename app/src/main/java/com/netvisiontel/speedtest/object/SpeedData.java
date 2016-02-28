package com.netvisiontel.speedtest.object;

/**
 * Created by ngodi on 2/19/2016.
 */
public class SpeedData {
    int mode = 0;
    String speed;

    public SpeedData(int mode) {
        this.mode = mode;
    }

    public SpeedData(int mode, String speed) {
        this.mode = mode;
        this.speed = speed;
    }

    public int getMode() {
        return mode;
    }

    public String getSpeed() {
        return speed;
    }

}
