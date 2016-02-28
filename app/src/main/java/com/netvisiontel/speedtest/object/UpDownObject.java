package com.netvisiontel.speedtest.object;

/**
 * Created by ngodi on 2/26/2016.
 */
public class UpDownObject {
    float max;
    float avg;

    public UpDownObject(float max, float avg) {
        this.max = max;
        this.avg = avg;
    }

    public float getMax() {
        return this.max;
    }

    public float getAvg() {
        return this.avg;
    }
}
