package com.greendot.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.core.LocalVpnService;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.ToastHelper;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.http.util.EncodingUtils;

import java.util.HashMap;

public class RechargeHandleActivity extends AppCompatActivity {

    private WebView webView;
    private AVLoadingIndicatorView loading;
    private CountDownTimer loadingHideTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_recharge_handle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initView();
        initRechargeAction();
    }

    private void initRechargeAction(){
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultTextEncodingName("utf-8");

        HashMap<String, String> rechargeData = getRechargeData();

        if(null!=rechargeData && rechargeData.size() > 0){
            String payUrl = rechargeData.get("payurl");
            String payData = rechargeData.get("paydata");

            webView.postUrl(payUrl, EncodingUtils.getBytes(payData, "BASE64"));
        }else{
            ToastHelper.error(RechargeHandleActivity.this, "请求参数错误，请返回重试");
        }
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("alipays://platformapi") || url.contains("platformapi/startapp")) {
                    startAlipayActivity(url);
                }else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingHideTick = new CountDownTimer(1800, 1800) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        loading.hide();
                    }
                };
                loadingHideTick.start();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        loading = findViewById(R.id.loading);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();;
            }
        });
    }

    private void startAlipayActivity(String url) {
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setComponent(null);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(null != this.loadingHideTick){
            this.loadingHideTick.cancel();
            this.loadingHideTick = null;
        }

        super.onDestroy();
    }

    private HashMap<String, String> getRechargeData(){
        Intent intent =getIntent();
        Bundle bundle= intent.getExtras();
        String rechargeParam = bundle.getString("rechargeData");

        if(null!=rechargeParam && !"".equals(rechargeParam)){
            JSONObject rechargeParamObj = JSONObject.parseObject(rechargeParam);

            String payurl = rechargeParamObj.getString("payurl");
            String uid = rechargeParamObj.getString("uid");
            String amount = rechargeParamObj.getString("amount");
            String type = rechargeParamObj.getString("type");
            String notifyurl = rechargeParamObj.getString("notifyurl");
            String returnurl = rechargeParamObj.getString("returnurl");
            String ordernum = rechargeParamObj.getString("ordernum");
            String orderuid = rechargeParamObj.getString("orderuid");
            String goodname = rechargeParamObj.getString("goodname");
            String key = rechargeParamObj.getString("key");

            StringBuffer rechargeInfo = new StringBuffer();
            rechargeInfo.append("uid="+uid + "&");
            rechargeInfo.append("amount="+amount + "&");
            rechargeInfo.append("type="+type + "&");
            rechargeInfo.append("notifyurl="+notifyurl + "&");
            rechargeInfo.append("returnurl="+returnurl + "&");
            rechargeInfo.append("ordernum="+ordernum + "&");
            rechargeInfo.append("orderuid="+orderuid + "&");
            rechargeInfo.append("goodname="+goodname + "&");
            rechargeInfo.append("key="+key);

            HashMap<String, String> result = new HashMap<String, String>();
            result.put("payurl", payurl);
            result.put("paydata", rechargeInfo.toString());

            return result;
        }

        return null;
    }
}
