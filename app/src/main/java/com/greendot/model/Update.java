package com.greendot.model;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonParseException;
import com.greendot.config.Constant;
import com.greendot.util.EncryptHelper;

import java.text.MessageFormat;

public class Update {

    private int id;

    private String version;

    private String pubdate;

    private String device;

    private String downloadlink;

    private String updatecontent;

    private String remark;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDownloadlink() {
        return downloadlink;
    }

    public void setDownloadlink(String downloadlink) {
        this.downloadlink = downloadlink;
    }

    public String getUpdatecontent() {
        return updatecontent;
    }

    public void setUpdatecontent(String updatecontent) {
        this.updatecontent = updatecontent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private static Update instance = null;

    public static Update instance()
    {
        if (instance == null)
        {
            instance = new Update();
        }
        return instance;
    }

    public void clearUpdate(){
        instance = null;
    }

    public void setUpdate(JSONObject server){

        int id = Integer.parseInt(String.valueOf(server.get("id")));
        String version = String.valueOf(server.get("version"));
        String pubdate = String.valueOf(server.get("pubdate"));
        String device = String.valueOf(server.get("device"));
        String downloadlink = String.valueOf(server.get("downloadlink"));
        String updatecontent = String.valueOf(server.get("updatecontent"));
        String remark = String.valueOf(server.get("remark"));

        this.setId(id);
        this.setVersion(version);
        this.setPubdate(pubdate);
        this.setDevice(device);
        this.setDownloadlink(downloadlink);
        this.setUpdatecontent(updatecontent);
        this.setRemark(remark);

    }
}