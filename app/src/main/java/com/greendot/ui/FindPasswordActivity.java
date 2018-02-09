package com.greendot.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class FindPasswordActivity extends AppCompatActivity {

    private Button btnSubmit;
    private TextView txtPassword;
    private TextView txtRePassword;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;
    private String phoneVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_find_password);

        Intent preIntent = getIntent();
        phoneVal = preIntent.getStringExtra("phone");

        if (null == phoneVal || "".equals(phoneVal)){
            Intent intent = new Intent();
            intent.setClass(FindPasswordActivity.this, RegisterPhoneActivity.class);
            FindPasswordActivity.this.startActivity(intent);
        }else{
            initView();
            initAction();
        }
    }

    private void initAction(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordVal = txtPassword.getText().toString();
                if (passwordVal.length() > 0){
                    passwordVal = passwordVal.trim();
                }

                if ("".equals(passwordVal)){
                    ToastHelper.warning(FindPasswordActivity.this, "请输入新登录密码");
                    return;
                }

                if(!ValidatorHelper.PasswordValidator(passwordVal)){
                    ToastHelper.warning(FindPasswordActivity.this, "登录密码格式不正确");
                    return;
                }

                String rePasswordVal = txtRePassword.getText().toString();
                if ("".equals(rePasswordVal)){
                    ToastHelper.warning(FindPasswordActivity.this, "请再次输入登录密码");
                    return;
                }

                if(!ValidatorHelper.PasswordValidator(rePasswordVal)){
                    ToastHelper.warning(FindPasswordActivity.this, "再次输入密码格式不正确");
                    return;
                }

                if (!passwordVal.equals(rePasswordVal)){
                    ToastHelper.warning(FindPasswordActivity.this, "两次输入密码不一致");
                    return;
                }


                final String phoneEncrypt = EncryptHelper.encrypt(phoneVal);
                final String passwordEncrypt = EncryptHelper.encrypt(passwordVal);
                final String rePasswordEncrypt = EncryptHelper.encrypt(rePasswordVal);

                handleLoading(true);

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleSubmit(phoneEncrypt, passwordEncrypt, rePasswordEncrypt);
                    }

                }).start();
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

        phoneVal = EncryptHelper.decrypt(phoneVal);
        toolbar.setSubtitle(phoneVal);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        loading = findViewById(R.id.loading);
        loadingMask = findViewById(R.id.loading_mask);
        txtPassword = (TextView) findViewById(R.id.txtPassword);
        txtRePassword = (TextView) findViewById(R.id.txtRePassword);
    }

    private void handleSubmit(final String phone, String password, String rePassword){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.FIND_PWD_API, false, FindPasswordActivity.this);

        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("phone", phone)
                .addParams("password", password)
                .addParams("repassword", rePassword)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(FindPasswordActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleLoading(false);
                        ToastHelper.error(FindPasswordActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {

                        String resultCode = String.valueOf(result.get("code"));
                        String resultContent = String.valueOf(result.get("content"));

                        if ("200".equals(resultCode)){

                            ToastHelper.success(FindPasswordActivity.this, "操作成功，请登录");

                            new Thread(new Runnable(){
                                public void run(){
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent();
                                    intent.putExtra("phone", phone);
                                    intent.setClass(FindPasswordActivity.this, LoginActivity.class);
                                    FindPasswordActivity.this.startActivity(intent);
                                }

                            }).start();
                        }else{
                            ToastHelper.warning(FindPasswordActivity.this, resultContent);
                        }
                        handleLoading(false);
                    }
                });
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){

            txtPassword.setFocusable(false);
            txtRePassword.setFocusable(false);

            loading.show();
            loadingMask.setVisibility(View.VISIBLE);
        }else{

            txtPassword.setFocusable(true);
            txtPassword.setFocusableInTouchMode(true);
            txtPassword.requestFocus();
            txtPassword.findFocus();

            txtRePassword.setFocusable(true);
            txtRePassword.setFocusableInTouchMode(true);
            txtRePassword.requestFocus();

            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
    }

}
