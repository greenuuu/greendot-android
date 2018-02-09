package com.greendot.util;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.greendot.model.Result;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;

import okhttp3.Response;

public abstract class ResponseHelper extends Callback<JSONObject>
{
    @Override
    public JSONObject parseNetworkResponse(Response response, int id) throws IOException
    {
        String str = response.body().string();
        JSONObject result = JSON.parseObject(str);
        return result;
    }
}
