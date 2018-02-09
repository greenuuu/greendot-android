package com.greendot.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.greendot.R;
import com.greendot.adapter.RechargeListAdapter;
import com.greendot.adapter.RechargeTypeListAdapter;
import com.greendot.config.Constant;
import com.greendot.core.LocalVpnService;
import com.greendot.model.Recharge;
import com.greendot.model.RechargeList;
import com.greendot.model.RechargeType;
import com.greendot.model.Server;
import com.greendot.model.ServerList;
import com.greendot.model.User;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.ToastHelper;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class RechargeActivity extends AppCompatActivity {

    private FloatingActionButton btnSubmit;
    private ListView rechargeListView;
    private TextView rechargeListEmptyHint;
    private ListView rechargeTypeListView;
    private AVLoadingIndicatorView loading;
    private LinearLayout loadingMask;

    private int currentSelectedTypeId = 1;
    private int currentSelectedRechargeId = 0;

    private int rechargeCheckDuration =  2000;
    private CountDownTimer rechargeCheckTick = null;

    private String rechargeOrderNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_recharge);

        initView();
        initRechargesInfo();
        initHintInfo();
        initAction();
    }

    private void initAction(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoading(true);
                new Thread(new Runnable(){
                    public void run(){
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleRechargeSubmit(currentSelectedRechargeId, currentSelectedTypeId);
                    }

                }).start();
            }
        });
    }

    private void initHintInfo(){
        Alerter.create(this)
            .setTitle("温馨提示")
            .setText(
                    "由于众所周知的原因，国外加速服务的IP地址，会定期进入GFW的封禁列表，导致绿点加速偶尔会出现服务不可用的情况。" +
                    "\n\n为了最大限度减少你在上述不可抗力情况下的损失，降低你的怒气，请谨慎成为付费用户。" +
                    "\n\n生容易，活容易，生活不容易。感谢你的支持！"
            )
            .setDuration(20000)
            .enableProgress(true)
            .setProgressColorRes(R.color.colorAccent)
            .setBackgroundColorRes(R.color.colorPrimaryDark)
            .show();
    }

    private void initRechargeChecker(){

        if(null != rechargeCheckTick){
            rechargeCheckTick.cancel();
            rechargeCheckTick = null;
        }

        rechargeCheckTick = new CountDownTimer(rechargeCheckDuration * 1000, rechargeCheckDuration) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(null != rechargeOrderNum && !"".equals(rechargeOrderNum)){
                    handleRechargeResultCheck();
                }
            }
            @Override
            public void onFinish() {}
        };

        rechargeCheckTick.start();
    }

    private void initRechargesInfo(){

        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.RECHARGE_LIST_API, true, RechargeActivity.this);
        OkHttpUtils
                .get()
                .headers(requestHelper.BuildRequestHeader())
                .url(requestUrl)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(RechargeActivity.this))
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastHelper.show(RechargeActivity.this, "系统错误，请稍候重试");
                    }

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        if ("200".equals(resultCode)){
                            JSONArray resultObj = (JSONArray) result.get("content");
                            handleRechargesGetSucceed(resultObj);
                        }else{
                            String resultContent = String.valueOf(result.get("content"));
                            ToastHelper.show(RechargeActivity.this, resultContent);
                        }
                    }
                });
    }

    private void  handleRechargeResultCheck(){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.RECHARGE_QUERY_API, true, RechargeActivity.this);
        OkHttpUtils
                .get()
                .headers(requestHelper.BuildRequestHeader())
                .addParams("ordernum", this.rechargeOrderNum)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(RechargeActivity.this))
                .url(requestUrl)
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {}

                    @Override
                    public void onResponse(JSONObject result, int id) {
                        String resultCode = String.valueOf(result.get("code"));
                        if ("200".equals(resultCode)){
                            JSONObject resultObj = (JSONObject) result.get("content");
                            handleRechargeResultSucceed(resultObj);
                        }
                    }
                });
    }

    private void handleRechargeResultSucceed(JSONObject result){

        this.rechargeCheckTick.cancel();
        this.rechargeCheckTick = null;

        String cash = result.getString("amount");
        AlertDialog dialogInstance = new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("恭喜你，成功续费" + cash + "元！感谢你的支持。")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RechargeActivity.this.finish();
                    }
                }).show();
        dialogInstance.setCanceledOnTouchOutside(false);
    }

    private void handleRechargesGetSucceed(JSONArray jsonResult){

        final RechargeList rechargeList = RechargeList.instance();
        rechargeList.clear();
        rechargeList.setRecharges(jsonResult);
        List<Recharge> recharges = rechargeList.getList();

        if(null!=recharges && recharges.size() > 0){

            currentSelectedRechargeId = recharges.get(0).getId();

            rechargeListEmptyHint.setVisibility(View.GONE);
            final RechargeListAdapter rechargeListAdapter = new RechargeListAdapter(RechargeActivity.this, recharges);
            rechargeListView.setAdapter(rechargeListAdapter);
            rechargeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg0, View viewItem, int position, long arg3) {
                    currentSelectedRechargeId = rechargeList.getRecharge(position).getId();
                    rechargeListAdapter.setSelectItem(position);
                    rechargeListAdapter.notifyDataSetInvalidated();
                }
            });
        }else{
            rechargeListEmptyHint.setVisibility(View.VISIBLE);
            rechargeListEmptyHint.setText("未获取到充值数据列表");
        }

        final List<RechargeType> rechargeTypes = getRechargeTypeList();
        final RechargeTypeListAdapter rechargeTypeListAdapter = new RechargeTypeListAdapter(RechargeActivity.this, rechargeTypes);
        rechargeTypeListView.setAdapter(rechargeTypeListAdapter);
        rechargeTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View viewItem, int position, long arg3) {
                currentSelectedTypeId = rechargeTypes.get(position).getId();
                rechargeTypeListAdapter.setSelectItem(position);
                rechargeTypeListAdapter.notifyDataSetInvalidated();
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
        btnSubmit = (FloatingActionButton) findViewById(R.id.btnSubmit);
        rechargeListView = (ListView) findViewById(R.id.lstRecharge);
        rechargeListEmptyHint = (TextView) findViewById(R.id.txtEmptyHint);
        rechargeTypeListView = (ListView) findViewById(R.id.lstRechargeType);
        loading = findViewById(R.id.loading);
        loadingMask = findViewById(R.id.loading_mask);
    }

    private void handleRechargeSubmit(int rechargeId, int typeId){

        if(rechargeId >=0){
            RequestHelper requestHelper = RequestHelper.instance();
            String requestUrl = requestHelper.BuildRequestUrl(Constant.RECHARGE_POST_API, true, RechargeActivity.this);
            User user = User.instance();

            OkHttpUtils
                    .post()
                    .url(requestUrl)
                    .headers(requestHelper.BuildRequestHeader())
                    .addParams("rid", rechargeId + "")
                    .addParams("tid", typeId + "")
                    .addParams("token", user.getToken())
                    .addParams("device", Constant.DEVICE)
                    .addParams("version", AppInfo.getVersionCode(RechargeActivity.this))
                    .build()
                    .execute(new ResponseHelper()
                    {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            handleLoading(false);
                            ToastHelper.error(RechargeActivity.this, "系统错误，请稍候重试");
                        }

                        @Override
                        public void onResponse(JSONObject result, int id) {
                            String resultCode = String.valueOf(result.get("code"));
                            JSONObject resultContent = (JSONObject) result.get("content");
                            if ("200".equals(resultCode)){
                                setRechargeOrderNum(resultContent);
                                initRechargeChecker();
                                handleRechargeSubmitSuccess(resultContent.toJSONString());
                            }else{
                                if(null != resultContent){
                                    ToastHelper.warning(RechargeActivity.this, resultContent.toJSONString());
                                }else{
                                    ToastHelper.warning(RechargeActivity.this, "支付请求失败，请稍候重试");
                                }
                            }
                            handleLoading(false);
                        }
                    });
        }
    }

    private void handleRechargeSubmitSuccess(String rechargeData){
        Intent intent = new Intent();
        intent.putExtra("rechargeData", rechargeData);
        intent.setClass(RechargeActivity.this, RechargeHandleActivity.class);
        RechargeActivity.this.startActivity(intent);
    }

    private void handleLoading(boolean isShow){
        if (isShow == true){
            loading.show();
            loadingMask.setVisibility(View.VISIBLE);
        }else{
            loading.hide();
            loadingMask.setVisibility(View.GONE);
        }
    }

    private List<RechargeType> getRechargeTypeList() {
        List<RechargeType> rechargeTypes = new ArrayList<RechargeType>();
        RechargeType rechargeTypeAli = new RechargeType();
        rechargeTypeAli.setId(1);
        rechargeTypeAli.setName("支付宝");
        rechargeTypeAli.setIcon("ic_ali");
        rechargeTypes.add(rechargeTypeAli);
        return rechargeTypes;
    }

    private void setRechargeOrderNum(JSONObject rechargeInfo){
        String orderNum = String.valueOf(rechargeInfo.get("ordernum"));
        this.rechargeOrderNum = orderNum;
    }

    @Override
    protected void onDestroy() {
        if( null != this.rechargeCheckTick){
            this.rechargeCheckTick.cancel();
            this.rechargeCheckTick = null;
        }
        super.onDestroy();
    }
}
