package com.netvisiontel.speedtest.Server;

import android.util.Log;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by ngodi on 2/20/2016.
 */
public class ServerData {
    public String TAG = "ServerData";
    public String url;
    public double lat;
    public double lon;
    public String name;
    public String country;
    public String cc;
    public String sponsor;
    public int id;
    public String url2;

    public ServerData(Node node) {
        Element eElement = (Element) node;
        url = eElement.getAttribute("url");
//        Log.d(TAG, "url: " + url);
        lat = Double.valueOf(eElement.getAttribute("lat"));
//        Log.d(TAG, "lat: " + lat);
        lon = Double.valueOf(eElement.getAttribute("lon"));
//        Log.d(TAG, "lon: " + lon);
        name = eElement.getAttribute("name");
//        Log.d(TAG, "name: " + name);
        country = eElement.getAttribute("country");
//        Log.d(TAG, "country: " + country);
        cc = eElement.getAttribute("cc");
//        Log.d(TAG, "cc: " + cc);
        sponsor = eElement.getAttribute("sponsor");
//        Log.d(TAG, "sponsor: " + sponsor);
        id = Integer.valueOf(eElement.getAttribute("id"));
//        Log.d(TAG, "id: " + id);
        url2 = eElement.getAttribute("url2");
//        Log.d(TAG, "url2: " + url2);
    }

    public String getUrl() {
        return  url;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return  name;
    }

    public String getCountry() {
        return  country;
    }

    public String getCc() {
        return  cc;
    }

    public String getSponsor() {
        return  sponsor;
    }

    public int getId() {
        return  id;
    }

    public String getUrl2() {
        return  url2;
    }

    public String getHost() {

        return url.split("//")[1].split("/")[0];

    }

    public String getUri() {
        return url.split("//")[1].split(url.split("//")[1].split("/")[0])[1];
    }

    public String getUriDownload() {
        String[] tmp = this.url.split("//")[1].split("/");
        String uri = "/";
        for(int i = 1; i < tmp.length - 1; i++)
            uri += (tmp[i] + "/");
        return  uri;
    }
}
