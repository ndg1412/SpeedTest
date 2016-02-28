package com.netvisiontel.speedtest.Interface;

/**
 * Created by ngodi on 2/24/2016.
 */
public interface IDownloadListener {
    public void onDownloadPacketsReceived(float transferRateBitPerSeconds, float download_avg);

    public void onDownloadError(int errorCode, String message);

    public void onDownloadProgress(int percent);
    public void onDownloadUpdate(float speed);
}
