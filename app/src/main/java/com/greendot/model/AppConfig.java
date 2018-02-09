package com.greendot.model;

import com.alibaba.fastjson.JSONObject;

public class AppConfig {

    private static AppConfig instance = null;

    public static AppConfig instance()
    {
        if (instance == null)
        {
            instance = new AppConfig();
        }
        return instance;
    }

    private String userGiftDay;

    private String inviteGiftDay;

    private String email;

    private String word;

    private String company;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUserGiftDay() {
        return userGiftDay;
    }

    public void setUserGiftDay(String userGiftDay) {
        this.userGiftDay = userGiftDay;
    }

    public String getInviteGiftDay() {
        return inviteGiftDay;
    }

    public void setInviteGiftDay(String inviteGiftDay) {
        this.inviteGiftDay = inviteGiftDay;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setConfig(JSONObject config){
        String userGiftDay = String.valueOf(config.get("usergiftday"));
        String inviteGiftDay = String.valueOf(config.get("invitegiftday"));
        String email = String.valueOf(config.get("email"));
        String word = String.valueOf(config.get("siteword"));
        String company = String.valueOf(config.get("company"));

        this.setUserGiftDay(userGiftDay);
        this.setInviteGiftDay(inviteGiftDay);
        this.setEmail(email);
        this.setWord(word);
        this.setCompany(company);
    }
}
