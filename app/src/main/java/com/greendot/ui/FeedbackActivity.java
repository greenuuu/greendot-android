package com.greendot.ui;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.config.Constant;
import com.greendot.model.User;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.ToastHelper;
import com.wang.avi.AVLoadingIndicatorView;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.Call;

public class FeedbackActivity extends AppCompatActivity {

    private FloatingActionButton btnSubmit;
    private EditText txtFeedback;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;
    private boolean submitStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_feedback);

        initView();

        initAction();
    }

    private void initAction(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String feedbackVal = txtFeedback.getText().toString();

                if (feedbackVal.length() > 0){
                    feedbackVal = feedbackVal.trim();
                }

                if (feedbackVal.length() > 0){
                    feedbackVal = feedbackVal.trim();
                }

                if ("".equals(feedbackVal)){
                    ToastHelper.warning(FeedbackActivity.this, "请输入反馈内容");
                    return;
                }

                final String feedbackData = feedbackVal;

                handleLoading(true);

                btnSubmit.hide();

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleFeedback(feedbackData);
                    }

                }).start();
            }
        });
    }

    private void handleFeedback(String content){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.FEEDBACK_API, false, FeedbackActivity.this);
        User user = User.instance();
        OkHttpUtils
                .post()
                .url(requestUrl)
                .headers(requestHelper.BuildRequestHeader())
                .addParams("title", "来自安卓客户端的反馈")
                .addParams("target", Constant.DEVICE)
                .addParams("content", content)
                .addParams("token", user.getToken())
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(FeedbackActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleLoading(false);
                        ToastHelper.error(FeedbackActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        String resultContent = String.valueOf(result.get("content"));
                        if ("200".equals(resultCode)){
                            handleFeedbackSuccess();
                        }else{
                            ToastHelper.warning(FeedbackActivity.this, resultContent);
                        }
                        handleLoading(false);
                    }
                });
    }

    private void handleFeedbackSuccess(){
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("已成功提交反馈内容，我们会尽快处理并给予回复。")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FeedbackActivity.this.finish();
                    }
                }).show();
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){
            txtFeedback.setFocusable(false);

            loading.show();
            loadingMask.setVisibility(View.VISIBLE);
        }else{
            txtFeedback.setFocusable(true);
            txtFeedback.setFocusableInTouchMode(true);
            txtFeedback.requestFocus();
            txtFeedback.findFocus();

            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
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

        btnSubmit = (FloatingActionButton) findViewById(R.id.btnSubmit);
        txtFeedback = (EditText) findViewById(R.id.txtFeedback);
        loading = findViewById(R.id.loading);
        loadingMask = findViewById(R.id.loading_mask);
    }
}
