package com.greendot.model;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RechargeList{
    private static RechargeList instance = null;

    public static RechargeList instance()
    {
        if (instance == null)
        {
            instance = new RechargeList();
        }
        return instance;
    }

    private List<Recharge> recharges = new ArrayList<Recharge>();

    public void addRecharge(Recharge recharge){
        this.recharges.add(recharge);
    }

    public int getCount(){
        return this.recharges.size();
    }

    public void clear(){
        this.recharges.clear();
    }

    public void setRecharges(JSONArray jsonArray){
        int len = jsonArray.size();
        for(int i =0; i < len; i++){
            JSONObject serverItem = (JSONObject) jsonArray.get(i);

            Recharge recharge = new Recharge();
            recharge.setRecharge(serverItem);
            recharges.add(recharge);
        }
    }

    public Recharge getRecharge(int index){
        return this.recharges.get(index);
    }

    public List<Recharge> getList(){
        return this.recharges;
    }
}