package com.netvisiontel.speedtest.Server;

import android.content.Context;
import android.util.Log;

import com.netvisiontel.speedtest.R;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by ngodi on 2/20/2016.
 */
public class LoadServer {

    Context context;
    private static final String TAG = "LoadServer";
    ArrayList<ServerData> alServer = new ArrayList<ServerData>();

    public LoadServer(Context ctx) {
        try {
            InputStream inputStream = ctx.getResources().openRawResource(R.raw.speedtest_servers_static);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();
            Document doc = null;
            doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();

            Element eRoot = doc.getDocumentElement();
            NodeList nServer = doc.getElementsByTagName("server");
            for (int i = 0; i < nServer.getLength(); i++) {

                Node node = nServer.item(i);
                if(node.hasAttributes()) {
                    ServerData sd = new ServerData(node);
                    alServer.add(sd);
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ServerData> getServerInCountry(String country) {
        ArrayList<ServerData> alCountry = new ArrayList<ServerData>();
        for(int i = 0; i < alServer.size(); i++) {
            ServerData sd = alServer.get(i);
            if(sd.getCountry().equals(country)) {
                alCountry.add(sd);
                Log.d(TAG, "getServerInCountry: url: " + sd.getUrl());
            }
        }

        return (alCountry.size() > 0) ? alCountry : null;
    }
}
