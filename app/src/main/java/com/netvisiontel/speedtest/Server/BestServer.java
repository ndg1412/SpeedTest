package com.netvisiontel.speedtest.Server;

import com.netvisiontel.speedtest.util.Network;

import java.util.ArrayList;

/**
 * Created by ngodi on 2/22/2016.
 */
public class BestServer {
    public static ServerData getBestServer(ArrayList<ServerData> server) {
        int save = 0;
        float time_ping = Network.pingUrl(server.get(0).getHost());

        for(int i = 1; i < server.size(); i++) {
            float time = Network.pingUrl(server.get(i).getHost());
            if(time_ping > time) {
                time_ping = time;
                save = i;
            }
        }
        return server.get(save);
    }
}
