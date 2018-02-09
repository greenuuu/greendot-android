package com.greendot.model;
import com.alibaba.fastjson.JSONObject;
import com.greendot.util.EncryptHelper;
import java.text.MessageFormat;

public class Server {

    private int id;

    private String host;

    private String icon;

    private String password;

    private String method;

    private String name;

    private String remark;

    private String port;

    private int linkCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        if(null!=host && !"".equals(host)){
            String[] hosts = host.split("\\.");
            if(hosts.length > 0){
                return hosts[0] + "." + hosts[1] + "." + hosts[2] + "." + "*";
            }
            return "";
        }
        return "";
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setServer(JSONObject server){

        int id = Integer.parseInt(String.valueOf(server.get("id")));
        String host = String.valueOf(server.get("host"));
        String icon = String.valueOf(server.get("icon"));
        String password = String.valueOf(server.get("password"));
        String method = String.valueOf(server.get("method"));
        String name = String.valueOf(server.get("name"));
        String remark = String.valueOf(server.get("remark"));
        String port = String.valueOf(server.get("port"));

        host = EncryptHelper.decrypt(host);
        password = EncryptHelper.decrypt(password);
        port = EncryptHelper.decrypt(port);
        method = EncryptHelper.decrypt(method);


        this.setId(id);
        this.setHost(host);
        this.setIcon(icon);
        this.setPassword(password);
        this.setMethod(method);
        this.setName(name);
        this.setRemark(remark);
        this.setPort(port);
    }

    public String getServerProxyUrl(){
        // ss://method:password@host:port

        // ss://aes-256-cfb:ZDBlYzQ2Yj@66.112.212.147:443

        String proxyUrl = MessageFormat.format("ss://{0}:{1}@{2}:{3}",this.getMethod(), this.getPassword(), this.host, this.getPort());
        return proxyUrl;
    }
}