package com.greendot.model;

import com.alibaba.fastjson.JSONObject;
import com.greendot.util.EncryptHelper;

public class Recharge {
    private int id;

    private String cash;

    private int month;

    private String remark;

    private String discount;

    private String name;

    private String discountcash;

    private int ordernum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscountcash() {
        return discountcash;
    }

    public void setDiscountcash(String discountcash) {
        this.discountcash = discountcash;
    }

    public int getOrdernum() {
        return ordernum;
    }

    public void setOrdernum(int ordernum) {
        this.ordernum = ordernum;
    }

    public void setRecharge(JSONObject recharge){

        int id = Integer.parseInt(String.valueOf(recharge.get("id")));
        String cash = String.valueOf(recharge.get("cash"));
        int month = Integer.parseInt(String.valueOf(recharge.get("month")));
        String discount = String.valueOf(recharge.get("discount"));
        String discountcash = String.valueOf(recharge.get(("discountcash")));
        String remark = String.valueOf(recharge.get("remark"));
        String name = String.valueOf(recharge.get("name"));
        int ordernum = Integer.parseInt(String.valueOf(recharge.get("ordernum")));

        this.setId(id);
        this.setCash(cash);
        this.setMonth(month);
        this.setDiscount(discount);
        this.setDiscountcash(discountcash);
        this.setRemark(remark);
        this.setName(name);
        this.setOrdernum(ordernum);
    }
}
