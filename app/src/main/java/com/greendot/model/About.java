package com.greendot.model;

import com.alibaba.fastjson.JSONObject;

public class About {
    private int id;

    private String title;

    private String icon;

    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAbout(JSONObject about){
        int id = Integer.parseInt(String.valueOf(about.get("id")));
        String title = String.valueOf(about.get("title"));
        String content = String.valueOf(about.get("content"));
        String icon = String.valueOf(about.get("icon"));
        this.setId(id);
        this.setTitle(title);
        this.setContent(content);
        this.setIcon(icon);
    }
}
