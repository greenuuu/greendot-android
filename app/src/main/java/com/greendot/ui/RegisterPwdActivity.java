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

import org.w3c.dom.Text;

import okhttp3.Call;

public class RegisterPwdActivity extends AppCompatActivity {

    private Button btnSubmit;
    private TextView txtPassword;
    private TextView txtRePassword;
    private TextView txtInviteCode;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;
    private String phoneVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_register_pwd);

        Intent preIntent = getIntent();
        phoneVal = preIntent.getStringExtra("phone");

        if (null == phoneVal || "".equals(phoneVal)){
            Intent intent = new Intent();
            intent.setClass(RegisterPwdActivity.this, RegisterPhoneActivity.class);
            RegisterPwdActivity.this.startActivity(intent);
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
                    ToastHelper.warning(RegisterPwdActivity.this, "请输入密码");
                    return;
                }

                if(!ValidatorHelper.PasswordValidator(passwordVal)){
                    ToastHelper.warning(RegisterPwdActivity.this, "密码格式不正确");
                    return;
                }

                String rePasswordVal = txtRePassword.getText().toString();
                if ("".equals(rePasswordVal)){
                    ToastHelper.warning(RegisterPwdActivity.this, "请再次输入密码");
                    return;
                }

                if(!ValidatorHelper.PasswordValidator(rePasswordVal)){
                    ToastHelper.warning(RegisterPwdActivity.this, "再次输入密码格式不正确");
                    return;
                }

                if (!passwordVal.equals(rePasswordVal)){
                    ToastHelper.warning(RegisterPwdActivity.this, "两次输入密码不一致");
                    return;
                }

                String inviteCodeVal = txtInviteCode.getText().toString();
                if (inviteCodeVal.length() > 0){
                    inviteCodeVal = inviteCodeVal.trim();
                }

                if (!"".equals(inviteCodeVal)){
                    if (!ValidatorHelper.InviteCodeValidator(inviteCodeVal)){
                        ToastHelper.warning(RegisterPwdActivity.this, "邀请码格式不正确");
                        return;
                    }
                }

                final String phoneEncrypt = EncryptHelper.encrypt(phoneVal);
                final String passwordEncrypt = EncryptHelper.encrypt(passwordVal);
                final String rePasswordEncrypt = EncryptHelper.encrypt(rePasswordVal);
                if (!"".equals(inviteCodeVal)){
                    inviteCodeVal = EncryptHelper.encrypt(inviteCodeVal);
                }

                final String inviteCodeEncrypt = inviteCodeVal;

                handleLoading(true);

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleRegister(phoneEncrypt, passwordEncrypt, rePasswordEncrypt, inviteCodeEncrypt);
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
        txtInviteCode = (TextView) findViewById(R.id.txtInviteCode);
    }

    private void handleRegister(final String phone, String password, String rePassword, String code){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.REGISTER_PWD_API, false, RegisterPwdActivity.this);

        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("phone", phone)
                .addParams("password", password)
                .addParams("repassword", rePassword)
                .addParams("invitecode", code)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(RegisterPwdActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleLoading(false);
                        ToastHelper.error(RegisterPwdActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {

                        String resultCode = String.valueOf(result.get("code"));
                        String resultContent = String.valueOf(result.get("content"));

                        if ("200".equals(resultCode)){

                            ToastHelper.success(RegisterPwdActivity.this, "注册成功，请登录");

                            new Thread(new Runnable(){
                                public void run(){
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent();
                                    intent.putExtra("phone", phone);
                                    intent.setClass(RegisterPwdActivity.this, LoginActivity.class);
                                    RegisterPwdActivity.this.startActivity(intent);
                                }

                            }).start();
                        }else{
                            ToastHelper.warning(RegisterPwdActivity.this, resultContent);
                        }
                        handleLoading(false);
                    }
                });
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){

            txtPassword.setFocusable(false);
            txtRePassword.setFocusable(false);
            txtInviteCode.setFocusable(false);

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

            txtInviteCode.setFocusable(true);
            txtInviteCode.setFocusableInTouchMode(true);
            txtInviteCode.requestFocus();

            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
    }
}
