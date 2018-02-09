package com.greendot.model;

import com.alibaba.fastjson.JSONObject;
import com.greendot.util.DateHelper;

public class User {

    private static User instance = null;

    public static User instance()
    {
        if (instance == null)
        {
            instance = new User();
        }
        return instance;
    }

    private int id;

    private String token;

    private String phone;

    private String expireTime;

    private String avatar;

    private int type;

    private String adCode;

    private boolean expired;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneMask(){
        if (null!=phone &&  !"".equals(phone)){
            return phone.substring(0, 3) + "****" + phone.substring(7, phone.length());
        }
        return "";
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isLoginIn(){
        if (null == this.token || "".equals(this.token)){
            return false;
        }

        return true;
    }

    public String getExpireDate(){
        if (null != this.expireTime && !"".equals(this.expireTime)){
            return DateHelper.getFormatDateStr(this.expireTime, "");
        }
        return "";
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }


    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setUser(JSONObject userInfo){
        String phone = String.valueOf(userInfo.get("phone"));
        String avatar = String.valueOf(userInfo.get("avatar"));
        int type = Integer.parseInt(String.valueOf(userInfo.get("type")));
        String expire = String.valueOf(userInfo.get("serviceexpiredate"));
        String token = String.valueOf(userInfo.get("token"));
        String adCode = String.valueOf(userInfo.get("adcode"));
        int id = Integer.parseInt(String.valueOf(userInfo.get("id")));
        boolean expired = Boolean.parseBoolean(String.valueOf(userInfo.get("expired")));

        this.setType(type);
        this.setToken(token);
        this.setAvatar(avatar);
        this.setPhone(phone);
        this.setExpireTime(expire);
        this.setAdCode(adCode);
        this.setId(id);
        this.setExpired(expired);
    }
}
