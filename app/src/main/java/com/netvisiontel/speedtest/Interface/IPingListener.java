package com.netvisiontel.speedtest.Interface;

/**
 * Created by ngodi on 2/19/2016.
 */
public interface IPingListener {
    public void onPingReceived(float time, String loss);

    public void onPingProgress(int percent);

    public void onPingError(int errorCode, String message);
}
