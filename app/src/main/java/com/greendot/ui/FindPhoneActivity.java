package com.greendot.ui;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.EncryptHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.ToastHelper;
import com.greendot.util.ValidatorHelper;
import com.wang.avi.AVLoadingIndicatorView;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.Call;

public class FindPhoneActivity extends AppCompatActivity {

    private TextView txtPhone;
    private TextView txtCode;
    private Button btnSubmit;
    private Button btnCode;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;
    private boolean codeSendflag = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_find_phone);

        initView();

        initAction();
    }

    private void initAction(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneVal = txtPhone.getText().toString();
                if (phoneVal.length() > 0){
                    phoneVal = phoneVal.trim();
                }

                if ("".equals(phoneVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入手机号码");
                    return;
                }

                if(!ValidatorHelper.PhoneValidator(phoneVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入正确的手机号码");
                    return;
                }

                if (codeSendflag == false){
                    ToastHelper.warning(FindPhoneActivity.this, "请获取短信验证码");
                    return;
                }

                String codeVal = txtCode.getText().toString();
                if (codeVal.length() > 0){
                    codeVal = codeVal.trim();
                }

                if ("".equals(codeVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入短信验证码");
                    return;
                }

                if(!ValidatorHelper.CodeValidator(codeVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入正确的验证码");
                    return;
                }

                final String phoneData = EncryptHelper.encrypt(phoneVal);
                final String codeData = EncryptHelper.encrypt(codeVal);

                handleLoading(true);

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleSubmit(phoneData, codeData);
                    }

                }).start();
            }
        });

        btnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneVal = txtPhone.getText().toString();
                if (phoneVal.length() > 0){
                    phoneVal = phoneVal.trim();
                }

                if ("".equals(phoneVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入手机号码");
                    return;
                }

                if(!ValidatorHelper.PhoneValidator(phoneVal)){
                    ToastHelper.warning(FindPhoneActivity.this, "请输入正确的手机号码");
                    return;
                }

                final String phoneEncrypt = EncryptHelper.encrypt(phoneVal);

                handleLoading(true);

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleCodeSend(phoneEncrypt);
                    }

                }).start();

            }
        });
    }

    private void handleSubmit(final String phone, String code){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.FIND_PHONE_API, false, FindPhoneActivity.this);

        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("phone", phone)
                .addParams("code", code)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(FindPhoneActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleLoading(false);
                        ToastHelper.error(FindPhoneActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        String resultContent = String.valueOf(result.get("content"));
                        if ("200".equals(resultCode)){
                            countDownTimer.cancel();
                            Intent intent = new Intent();
                            intent.putExtra("phone",phone);
                            intent.setClass(FindPhoneActivity.this, FindPasswordActivity.class);
                            FindPhoneActivity.this.startActivity(intent);
                        }else{
                            ToastHelper.warning(FindPhoneActivity.this, resultContent);
                        }
                        handleLoading(false);
                    }
                });
    }

    private void handleCodeSend(String phone){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.FIND_CODE_API, false, FindPhoneActivity.this);

        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("phone", phone)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(FindPhoneActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleLoading(false);
                        ToastHelper.error(FindPhoneActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        String resultContent = String.valueOf(result.get("content"));
                        if ("200".equals(resultCode)){
                            codeSendflag = true;
                            btnCode.setEnabled(false);
                            final int codeCountDown = Integer.parseInt(resultContent);
                            countDownTimer = new CountDownTimer(codeCountDown * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    long remainCount = millisUntilFinished / 1000;
                                    btnCode.setText(String.valueOf(remainCount) + "秒过期");
                                }
                                @Override
                                public void onFinish() {
                                    btnCode.setEnabled(true);
                                    btnCode.setText("获取验证码");
                                }
                            };

                            countDownTimer.start();
                        }else{
                            ToastHelper.warning(FindPhoneActivity.this, resultContent);
                        }
                        handleLoading(false);
                    }
                });
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();;
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnCode = (Button) findViewById(R.id.btnSendCode);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        txtCode = (TextView) findViewById(R.id.txtCode);

        loading = findViewById(R.id.loading);
        loadingMask = findViewById(R.id.loading_mask);
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){

            txtPhone.setFocusable(false);
            txtCode.setFocusable(false);

            loading.show();
            loadingMask.setVisibility(View.VISIBLE);
        }else{

            txtPhone.setFocusable(true);
            txtPhone.setFocusableInTouchMode(true);
            txtPhone.requestFocus();

            txtCode.setFocusable(true);
            txtCode.setFocusableInTouchMode(true);
            txtCode.requestFocus();
            txtPhone.findFocus();

            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
    }

}
