package com.greendot.ui;

import android.graphics.Bitmap;
import android.net.http.SslError;
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

import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.RequestHelper;
import com.wang.avi.AVLoadingIndicatorView;

public class AboutActivity extends AppCompatActivity {

    private WebView webView;
    private AVLoadingIndicatorView loading;
    private CountDownTimer loadingHideTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_about);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initView();
        initContent();
    }

    private void initContent(){
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultTextEncodingName("utf-8");

        String aboutUrl = RequestHelper.getServerRoot() + Constant.ABOUT_URL;

        webView.loadUrl(aboutUrl);
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
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

    @Override
    protected void onDestroy() {
        if(null != this.loadingHideTick){
            this.loadingHideTick.cancel();
            this.loadingHideTick = null;
        }

        super.onDestroy();
    }
}
