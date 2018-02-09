package com.greendot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.model.User;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.EncryptHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.SharedPreferencesHelper;
import com.greendot.util.ToastHelper;
import com.greendot.util.ValidatorHelper;
import com.wang.avi.AVLoadingIndicatorView;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView txtRegister;
    private TextView txtFind;
    private EditText txtPhone;
    private EditText txtPassword;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;
    private SharedPreferencesHelper store;
    private long backExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_login);

        initView();

        initAction();
    }

    private void initAction(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneVal = txtPhone.getText().toString();
                String passwordVal = txtPassword.getText().toString();

                if (phoneVal.length() > 0){
                    phoneVal = phoneVal.trim();
                }

                if (passwordVal.length() > 0){
                    passwordVal = passwordVal.trim();
                }

                if ("".equals(phoneVal)){
                    ToastHelper.warning(LoginActivity.this, "请输入手机号码");
                    return;
                }

                if(!ValidatorHelper.PhoneValidator(phoneVal)){
                    ToastHelper.warning(LoginActivity.this, "请输入正确的手机号码");
                    return;
                }

                if ("".equals(passwordVal)){
                    ToastHelper.warning(LoginActivity.this, "请输入登录密码");
                    return;
                }

                if(!ValidatorHelper.PasswordValidator(passwordVal)){
                    ToastHelper.warning(LoginActivity.this, "请输入正确的登录密码");
                    return;
                }

                final String phoneEncrypt = EncryptHelper.encrypt(phoneVal);
                final String passwordEncrpt = EncryptHelper.encrypt(passwordVal);

                handleLoading(true);

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleLogin(phoneEncrypt, passwordEncrpt);
                    }

                }).start();
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterPhoneActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        txtFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, FindPhoneActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        txtFind = (TextView) findViewById(R.id.txtFind);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        loading = findViewById(R.id.loading);
        loadingMask = findViewById(R.id.loading_mask);
        store = new SharedPreferencesHelper(getApplicationContext());

        String phone = store.getString("phone");
        String password = store.getString("password");

        if (null!=phone && !"".equals(phone)){
            phone = EncryptHelper.decrypt(phone);
            txtPhone.setText(phone);
            if (null!=password && !"".equals(password)){
                password = EncryptHelper.decrypt(password);
                txtPassword.setText(password);
            }
        }

        Intent intent = getIntent();
        String registerPhone = intent.getStringExtra("phone");

        if (null != registerPhone && !"".equals(registerPhone)){
            registerPhone = EncryptHelper.decrypt(registerPhone);
            txtPhone.setText(registerPhone);
        }
    }

    private void handleLogin(String phone, String password){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.USER_LOGIN_API, false, LoginActivity.this);

        OkHttpUtils
            .post()
            .url(requestUrl)
            .headers(requestHelper.BuildRequestHeader())
            .addParams("phone", phone)
            .addParams("password", password)
            .addParams("device", Constant.DEVICE)
            .addParams("version", AppInfo.getVersionCode(LoginActivity.this))
            .build()
            .execute(new ResponseHelper()
            {
                @Override
                public void onError(Call call, Exception e, int id) {
                    handleLoading(false);
                    ToastHelper.error(LoginActivity.this, "系统错误，请稍候重试");
                }

                @Override
                public void onResponse(JSONObject result, int id) {

                    String resultCode = String.valueOf(result.get("code"));

                    if ("200".equals(resultCode)){
                        JSONObject resultObj = (JSONObject) result.get("content");
                        handleLoginSucceed(resultObj);
                    }else{
                        String resultContent = String.valueOf(result.get("content"));
                        ToastHelper.warning(LoginActivity.this, resultContent);
                    }

                    handleLoading(false);
                }
            });
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){
            txtPhone.setFocusable(false);
            txtPassword.setFocusable(false);

            loading.show();
            loadingMask.setVisibility(View.VISIBLE);
        }else{
            txtPhone.setFocusable(true);
            txtPhone.setFocusableInTouchMode(true);
            txtPhone.requestFocus();
            txtPhone.findFocus();

            txtPassword.setFocusable(true);
            txtPassword.setFocusableInTouchMode(true);
            txtPassword.requestFocus();

            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
    }

    private void handleLoginSucceed(JSONObject result){

        User user = User.instance();
        user.setUser(result);

        String password = txtPassword.getText().toString();
        password = EncryptHelper.encrypt(password);
        String phone = EncryptHelper.encrypt(user.getPhone());
        store.put("phone", phone);
        store.put("password", password);

        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ((System.currentTimeMillis() - backExitTime) > 2000) {
                ToastHelper.warning(LoginActivity.this, "再按一次退出应用");
                backExitTime = System.currentTimeMillis();
            } else {
                ApplicationHelper.getInstance().exit(LoginActivity.this);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
