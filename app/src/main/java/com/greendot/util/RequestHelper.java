package com.greendot.util;
import android.content.Context;

import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.model.User;

import java.util.HashMap;
import java.util.Map;

public class RequestHelper {

    private static RequestHelper instance = null;

    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.3319.102 Safari/537.36";

    public static String SERVER_ROOT = null;

    public static String getServerRoot(){
        return SERVER_ROOT;
    }

    public static RequestHelper instance()
    {
        if (instance == null)
        {
            instance = new RequestHelper();
        }
        return instance;
    }

    public String BuildRequestUrl(String url, boolean isToken, Context context){
        String serverRoot = this.getServerRoot();
        if(null == serverRoot || "".equals(serverRoot)){
            serverRoot = context.getResources().getString(R.string.app_site);
            serverRoot = "http://" + serverRoot + "/";
        }

        String requestUrl = serverRoot + url;

        if (requestUrl.contains("?")){
            requestUrl += "&device=" + Constant.DEVICE;
        }else{
            requestUrl += "?device=" + Constant.DEVICE;
        }

        requestUrl += "&version=" + AppInfo.getVersionCode(context);

        User user = User.instance();
        String userToken = user.getToken();

        if (!"".equals(userToken) && isToken == true){
            requestUrl += "&token=" + userToken;
        }

        return requestUrl;
    }

    public Map<String, String> BuildRequestHeader(){
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", USER_AGENT);
        return headers;
    }
}
