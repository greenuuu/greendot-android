package com.greendot.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.model.AppConfig;
import com.greendot.model.Update;
import com.greendot.model.User;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.EncryptHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.SharedPreferencesHelper;
import com.greendot.util.ToastHelper;
import com.tapadoo.alerter.Alerter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class SplashActivity extends AppCompatActivity {

    private ImageView imgLogo;
    private SharedPreferencesHelper store;
    private int animateDuration = 2000;
    private MainAsyncTask mainTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ApplicationHelper.getInstance().addActivity(this);

        initView();
        initAnimation();

        mainTask.execute();
    }

    private void handleUpdateCheck(){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.CHECK_UPDATE_API, false, SplashActivity.this);

        OkHttpUtils
                .get()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(SplashActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        if ("200".equals(resultCode)){
                            JSONObject updateInfo = (JSONObject)result.get("content");
                            Update update = Update.instance();
                            update.setUpdate(updateInfo);
                        }
                    }
                });
    }

    private void handleGateway(){
        String configUrl = Constant.GATE_API;
        OkHttpUtils
            .get()
            .url(configUrl)
            .build()
            .execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {}

                @Override
                public void onResponse(String response, int id) {
                    if (null != response && !"".equals(response)){
                        String serverRoot = EncryptHelper.decrypt(response);
                        RequestHelper.SERVER_ROOT = serverRoot;
                    }
                    handleConfig();
                    handleUpdateCheck();
                }
            });
    }

    private void handleConfig(){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.CONFIG_API, false, SplashActivity.this);
        OkHttpUtils
                .get()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(SplashActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleConfigError();
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        if ("200".equals(resultCode)){
                            JSONObject resultObj = (JSONObject) result.get("content");

                            AppConfig config = AppConfig.instance();
                            config.setConfig(resultObj);

                            handleAuth();
                        }
                    }
                });
    }

    private void handleConfigError(){
        Alerter.create(this)
            .setTitle("系统提示")
            .setText(
                    "很抱歉的通知你，我们购买的垃圾服务器又被墙了！程序猿正在抓紧时间处理，并表示心累！ " +
                    "\n\n如果你是付费用户，我们会在服务恢复后，免费延长你一个月使用时间作为补偿。" +
                    "\n\n如果你是免费用户，为了降低你在这种不可抗力情况下的怒气，续费前请慎重考虑一下。" +
                    "\n\n最后，再次表达我们的歉意。顺祝大家早日肉身翻墙成功！")
            .setDuration(50000)
            .enableProgress(true)
            .setProgressColorRes(R.color.colorWhite)
            .setBackgroundColorRes(R.color.colorAccent)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ApplicationHelper.getInstance().exit(SplashActivity.this);
                }
            })
            .show();
    }

    private void initAnimation(){
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(animateDuration);
        animationSet.addAnimation(alphaAnimation);
        imgLogo.setAnimation(animationSet);
    }

    private void handleAuth(){
        String phone = store.getString("phone");
        String password = store.getString("password");
        if (null!=phone && null!=password && !"".equals(phone) && !"".equals(password)){
            handleLogin(phone, password);
        }else{
            redirectLoginActivity();
        }
    }

    private void handleLogin(String phone, String password){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.USER_LOGIN_API, false, SplashActivity.this);

        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("phone", phone)
                .addParams("password", password)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(SplashActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        redirectLoginActivity();
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        if ("200".equals(resultCode)){
                            JSONObject resultObj = (JSONObject) result.get("content");
                            handleLoginSucceed(resultObj);
                        }else{
                            redirectLoginActivity();
                        }
                    }
                });
    }

    private void handleLoginSucceed(JSONObject result){
        User user = User.instance();
        user.setUser(result);
        redirectMainActivity();
    }

    private void redirectLoginActivity(){
        Intent intent = new Intent();
        intent.setClass(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(intent);
        this.finish();
    }

    private void redirectMainActivity(){
        Intent intent = new Intent();
        intent.setClass(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(intent);
        this.finish();
    }

    private void initView(){
        imgLogo = (ImageView)findViewById(R.id.imgLogo);
        store = new SharedPreferencesHelper(getApplicationContext());
        mainTask = new MainAsyncTask();

        Toasty.Config.getInstance()
                .setWarningColor(getResources().getColor(R.color.colorAccent))
                .setSuccessColor(getResources().getColor(R.color.colorPrimary))
                .apply();

    }

    private class MainAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;
            while (i <= 100) {
                try {
                    Thread.sleep( animateDuration / 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                publishProgress(i);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            handleGateway();
        }
    }
}
