package com.netvisiontel.speedtest.Interface;

/**
 * Created by ngodi on 2/20/2016.
 */
public interface IUploadListener {
    public void onUploadPacketsReceived(float transferRateBitPerSeconds, float upload_avg);

    public void onUploadError(int errorCode, String message);

    public void onUploadProgress(int percent);
}
