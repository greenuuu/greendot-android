package com.greendot.model;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerList{
    private static ServerList instance = null;

    public static ServerList instance()
    {
        if (instance == null)
        {
            instance = new ServerList();
        }
        return instance;
    }

    private List<Server> servers = new ArrayList<Server>();

    public void addServer(Server server){
        this.servers.add(server);
    }

    public int getCount(){
        return this.servers.size();
    }

    public void clear(){
        this.servers.clear();
    }

    public void setServers(JSONArray jsonArray){
        int len = jsonArray.size();
        for(int i =0; i < len; i++){
            JSONObject serverItem = (JSONObject) jsonArray.get(i);

            Server server = new Server();
            server.setServer(serverItem);
            servers.add(server);
        }
    }

    public Server getServer(int index){
        return this.servers.get(index);
    }

    public List<Server> getList(){
        return this.servers;
    }
}