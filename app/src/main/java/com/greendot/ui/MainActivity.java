package com.greendot.ui;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MailTo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.greendot.R;
import com.greendot.adapter.ServerListAdapter;
import com.greendot.config.Constant;
import com.greendot.model.AppConfig;
import com.greendot.model.Server;
import java.util.List;
import com.bumptech.glide.Glide;
import com.greendot.model.ServerList;
import com.greendot.model.Update;
import com.greendot.model.User;
import com.greendot.updater.Updater;
import com.greendot.updater.UpdaterConfig;
import com.greendot.util.AppInfo;
import com.greendot.util.ApplicationHelper;
import com.greendot.util.EncryptHelper;
import com.greendot.util.RequestHelper;
import com.greendot.util.ResponseHelper;
import com.greendot.util.SharedPreferencesHelper;
import com.greendot.util.ToastHelper;
import com.greendot.util.ValidatorHelper;
import com.john.waveview.WaveView;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;
import com.zhy.http.okhttp.OkHttpUtils;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;

import com.greendot.core.AppProxyManager;
import com.greendot.core.LocalVpnService;
import com.greendot.core.ProxyConfig;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MainActivity extends AppCompatActivity implements LocalVpnService.onStatusChangedListener{

    private ImageView userAvatar;
    private TextView userPhone;
    private ImageView userType;
    private TextView userExpireDate;
    private Toolbar toolbar;
    private ListView serverListView;
    private TextView serverListEmptyHint;

    private FloatingActionButton btnStart;
    private FABProgressCircle btnStartProgress;
    private WaveView progressWave;
    private LinearLayout progressWaveStop;

    private int currentRunningServerIndex = 0;
    private int durationDeterminate = 1000;
    private int currentSelectedServerIndex;
    private boolean currentRunning = false;
    private boolean btnStartStatus = false;
    private SharedPreferencesHelper store;
    private int userCheckDuration =  1000 * 10;
    private MyAsyncTask vpnTask;
    private boolean vpnGlobalMode = true;
    private CountDownTimer userCheckTick;
    private boolean userExpiredTag = false;
    private boolean userMessageTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_main);

        User user = User.instance();

        if (!user.isLoginIn()){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            this.finish();
        }else{
            initView();
            initUserInfo();
            initServersInfo();
            initActionInfo();
            initChecker();

            if (AppProxyManager.isLollipopOrAbove){
                new AppProxyManager(this);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Update update = Update.instance();
        String version = update.getVersion();

        if(null == version || "".equals(version)){
            menu.findItem(R.id.action_update).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_recharge:
                handleRecharge();
                break;
            case R.id.action_update:
                handleUpdate();
                break;
            case R.id.action_about:
                handleAbout();
                break;
            case R.id.action_feedback:
                handleFeedback();
                break;
            case R.id.action_qqgroup:
                handleQQGroup();
                break;
            case R.id.action_logout:
                handleLogout();
                break;
            case R.id.action_exit:
                handleExit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initActionInfo(){

        LocalVpnService.addOnStatusChangedListener(this);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = User.instance();
                if (user.isExpired()){
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("温馨提示")
                        .setMessage("服务已过期，请续费使用。")
                        .setPositiveButton("续费", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, RechargeActivity.class);
                                MainActivity.this.startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                }else{
                    if (btnStartStatus == false){
                        btnStartStatus = true;
                        if (LocalVpnService.IsRunning == true){
                            LocalVpnService.IsRunning = false;
                        }else{
                            handleVPNService();
                        }
                    }
                }
            }
        });

        btnStartProgress.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                ToastHelper.success(MainActivity.this, "已连接");
                btnStartStatus = false;
                progressWaveStop.setVisibility(View.GONE);
                progressWave.setProgress(100);
                progressWave.setVisibility(View.VISIBLE);
            }
        });
    }


    private void initUserInfo(){
        User user = User.instance();
        String phone = user.getPhoneMask();
        String expireDate = user.getExpireDate();
        String avatar = user.getAvatar();
        int type = user.getType();

        userPhone.setText(phone);
        userExpireDate.setText("服务过期：" + expireDate);

        if (null!=avatar && !"".equals(avatar)){
            Glide.with(this).load(avatar).apply(bitmapTransform(new CropCircleTransformation())).into(userAvatar);
        }

        if (type == 1){
            userType.setVisibility(View.VISIBLE);
        }else{
            userType.setVisibility(View.GONE);
        }
    }

    private void initServersInfo(){

        RequestHelper requestHelper = RequestHelper.instance();

        String requestUrl = requestHelper.BuildRequestUrl(Constant.SERVER_LIST_API, true, MainActivity.this);

        OkHttpUtils
            .get()
            .headers(requestHelper.BuildRequestHeader())
            .url(requestUrl)
            .build()
            .execute(new ResponseHelper()
            {
                @Override
                public void onError(Call call, Exception e, int id) {
                    ToastHelper.error(MainActivity.this, "系统错误，请稍候重试");
                }

                @Override
                public void onResponse(JSONObject result, int id) {
                    String resultCode = String.valueOf(result.get("code"));
                    if ("200".equals(resultCode)){
                        JSONArray resultObj = (JSONArray) result.get("content");
                        handleServerGetSucceed(resultObj);
                    }else{
                        String resultContent = String.valueOf(result.get("content"));
                        ToastHelper.warning(MainActivity.this, resultContent);
                    }
                }
            });
    }

    private void initChecker(){
        userCheckTick = new CountDownTimer(userCheckDuration * 6 * 60 * 24 * 10, userCheckDuration) {
            @Override
            public void onTick(long millisUntilFinished) {
                handleCheck();
            }
            @Override
            public void onFinish() {}
        };

        userCheckTick.start();
    }

    private void handleCheck(){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = requestHelper.BuildRequestUrl(Constant.USER_INFO_API, true, MainActivity.this);

        OkHttpUtils
            .get()
            .headers(requestHelper.BuildRequestHeader())
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
                        User user = User.instance();
                        String strAvatar = String.valueOf(resultObj.get("avatar"));
                        String strPhone = String.valueOf(resultObj.get("phone"));
                        String strType = String.valueOf(resultObj.get("type"));


                        String strMessageId = null;
                        if(null != resultObj.get("message_id")){
                            strMessageId= String.valueOf(resultObj.get("message_id"));
                        }

                        String strMessageContent = null;
                        if(null!=resultObj.get("message_content")){
                            strMessageContent = String.valueOf(resultObj.get("message_content"));
                        }

                        if (!user.getAvatar().equals(strAvatar)){
                            user.setAvatar(strAvatar);
                            Glide.with(MainActivity.this).load(strAvatar).apply(bitmapTransform(new CropCircleTransformation())).into(userAvatar);
                        }

                        if (!user.getPhone().equals(strPhone)){
                            user.setPhone(strPhone);
                            userPhone.setText(strPhone);
                        }

                        if ("1".equals(strType)){
                            userType.setVisibility(View.VISIBLE);
                        }else{
                            userType.setVisibility(View.GONE);
                        }

                        user.setUser(resultObj);
                        userExpireDate.setText("服务过期：" + user.getExpireDate());

                        if (user.isExpired()){
                            if(LocalVpnService.IsRunning == true){
                                LocalVpnService.IsRunning = false;
                                currentRunning = false;
                            }

                            if(userExpiredTag == false){
                                ToastHelper.warning(MainActivity.this, "服务已过期，请续费使用");
                                userExpiredTag = true;
                            }
                        }else{
                            handleSystemNotice(strMessageId, strMessageContent);
                        }

                    }else if("403".equals(resultCode)){
                        if(LocalVpnService.IsRunning == true){
                            LocalVpnService.IsRunning = false;
                            currentRunning = false;
                        }
                        User user = User.instance();
                        user.setToken("");
                        store.remove("password");
                        userCheckTick.cancel();

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("温馨提示")
                                .setMessage("该账号已在其它设备登录。")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ApplicationHelper.getInstance().clear(MainActivity.this);
                                        Intent intent = new Intent();
                                        intent.setClass(MainActivity.this, LoginActivity.class);
                                        MainActivity.this.startActivity(intent);
                                        MainActivity.this.finish();
                                    }
                                }).show();
                    }
                }
            });
    }

    private void handleSystemNotice(final String messageId, final String messageContent){
        if(userMessageTag == false){
            String messageStr = messageContent;
            if(null!=messageId && null!=messageStr && !"".equals(messageId) && !"".equals(messageStr)){
                String isMessageRead = store.getString(messageId);
                if(null==isMessageRead || "".equals(isMessageRead)){
                    userMessageTag = true;
                    messageStr = messageStr.replace("<br/>", "\n");
                    Alerter.create(this)
                        .setTitle("系统提示")
                        .setText(messageStr)
                        .setDuration(50000)
                        .enableProgress(true)
                        .setProgressColorRes(R.color.colorAccent)
                        .setBackgroundColorRes(R.color.colorPrimaryDark)
                        .setOnHideListener(new OnHideAlertListener() {
                            @Override
                            public void onHide() {
                                store.put(messageId, "1");
                            }
                        })
                        .show();
                }
            }
        }
    }

    private void handleVPNService() {
        Intent intent = LocalVpnService.prepare(this);
        if (intent == null) {
            btnStartProgress.show();
            startVPNService(currentSelectedServerIndex);
        } else {
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            btnStartProgress.show();
            startVPNService(currentSelectedServerIndex);
        } else {
            btnStartStatus = false;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void startVPNService(int serverIndex){
        ServerList servers = ServerList.instance();
        Server server = servers.getServer(serverIndex);
        String proxyUrl = server.getServerProxyUrl();
        if (ValidatorHelper.ProxyUrlValidator(proxyUrl)){
            LocalVpnService.ProxyUrl = proxyUrl;
            ProxyConfig.Instance.globalMode = vpnGlobalMode;
            startService(new Intent(this, LocalVpnService.class));
        }else{
            ToastHelper.error(MainActivity.this, "配置错误，请稍候重试");
        }
    }

    @Override
    public void onLogReceived(String logString) {
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        currentRunning = isRunning;
        if (currentRunning == false){
            handleVpnStatusChange();
        }else{
            vpnTask = new MyAsyncTask();
            vpnTask.execute();
        }
    }

    private void handleVpnStatusChange(){
        currentRunningServerIndex = currentSelectedServerIndex;
        Server currentServer = ServerList.instance().getServer(currentRunningServerIndex);
        int currentServerId = currentServer.getId();
        if (currentRunning){
            btnStartProgress.beginFinalAnimation();
            handleServerCount(currentServerId, true);
        }else{
            progressWaveStop.setVisibility(View.VISIBLE);
            progressWave.setProgress(0);
            progressWave.setVisibility(View.GONE);
            ToastHelper.warning(MainActivity.this, "已断开连接");
            handleServerCount(currentServerId, false);
            btnStartStatus = false;
        }
    }

    private void handleServerGetSucceed(JSONArray jsonResult){
        ServerList serverList = ServerList.instance();
        serverList.clear();
        serverList.setServers(jsonResult);
        List<Server> servers = serverList.getList();
        if(null!=servers && servers.size() > 0){
            serverListEmptyHint.setVisibility(View.GONE);
            final ServerListAdapter serverListAdapter = new ServerListAdapter(MainActivity.this, servers);
            serverListView.setAdapter(serverListAdapter);
            serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg0, View viewItem, int position, long arg3) {
                    currentSelectedServerIndex = position;
                    serverListAdapter.setSelectItem(position);
                    serverListAdapter.notifyDataSetInvalidated();
                }
            });
        }else{
            serverListEmptyHint.setVisibility(View.VISIBLE);
            serverListEmptyHint.setText("未获取到加速节点列表");
        }
    }

    private void handleQQGroup(){
        String key = getResources().getString(R.string.app_qq_group_key);
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            ToastHelper.warning(MainActivity.this, "未安装软件或版本不支持");
        }
    }

    private void handleUpdate(){
        Update update = Update.instance();
        final String updateDownloadLink = update.getDownloadlink();

        String updateContent = update.getUpdatecontent();
        updateContent = updateContent.replace("<br/>", "\n");

        if(null!=updateDownloadLink && !"".equals(updateDownloadLink)){
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("系统更新")
                .setMessage(updateContent)
                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean hasStoragePermission = checkStoragePermission();
                        if(hasStoragePermission){
                            ToastHelper.success(MainActivity.this, "更新文件下载中...");
                            Log.d("Kangming", updateDownloadLink);
                            try{
                                UpdaterConfig config = new UpdaterConfig.Builder(MainActivity.this)
                                    .setTitle(getResources().getString(R.string.app_name))
                                    .setDescription("更新文件下载中...")
                                    .setFileUrl(updateDownloadLink)
                                    .setCanMediaScanner(true)
                                    .setIsShowDownloadUI(true)
                                    .setNotificationVisibility(1)
                                    .setTitle(getResources().getString(R.string.app_name))
                                    .build();
                                Updater.get().showLog(false).download(config);
                            }catch(Exception e){
                                ToastHelper.warning(MainActivity.this, "文件下载异常，请稍候重试");
                            }
                        }else{
                            ToastHelper.warning(MainActivity.this, "请开启应用存储权限");
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
        }
    }

    private boolean checkStoragePermission(){
        String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
        int perm = MainActivity.this.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    private void handleRecharge(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, RechargeActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void handleFeedback(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, FeedbackActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void handleAbout(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, AboutActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void handleLogout(){
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("注销登录，将停止加速服务。确定注销吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (LocalVpnService.IsRunning == true){
                            LocalVpnService.IsRunning = false;
                            currentRunning = false;
                            handleServerCount(currentRunningServerIndex, false);
                        }

                        RequestHelper requestHelper = RequestHelper.instance();
                        String requestUrl = requestHelper.BuildRequestUrl(Constant.USER_LOGOUT_API, true, MainActivity.this);

                        final User user = User.instance();

                        OkHttpUtils
                            .post()
                            .headers(requestHelper.BuildRequestHeader())
                            .addParams("token", user.getToken())
                            .addParams("device", Constant.DEVICE)
                            .addParams("version", AppInfo.getVersionCode(MainActivity.this))
                            .url(requestUrl)
                            .build()
                            .execute(new ResponseHelper()
                            {
                                @Override
                                public void onError(Call call, Exception e, int id) {}

                                @Override
                                public void onResponse(JSONObject result, int id) {}
                            });

                        user.setToken("");
                        store.remove("password");
                        userCheckTick.cancel();
                        ApplicationHelper.getInstance().clear(MainActivity.this);
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    private void handleExit(){
        new AlertDialog.Builder(this)
            .setTitle("温馨提示")
            .setMessage("退出应用，将停止加速服务。确定退出吗？")
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (LocalVpnService.IsRunning == true){
                        LocalVpnService.IsRunning = false;
                        currentRunning = false;
                        handleServerCount(currentRunningServerIndex, false);
                    }

                    userCheckTick.cancel();

                    ApplicationHelper.getInstance().exit(MainActivity.this);
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        if(null!=userCheckTick){
            userCheckTick.cancel();
            userCheckTick = null;
        }
        super.onDestroy();
    }

    private void handleServerCount(int serverId, boolean isAdd){
        RequestHelper requestHelper = RequestHelper.instance();
        String requestUrl = null;
        if (isAdd == true){
            requestUrl = requestHelper.BuildRequestUrl(Constant.SERVER_CONNECT_API, true, MainActivity.this);
        }else{
            requestUrl = requestHelper.BuildRequestUrl(Constant.SERVER_DISCONNECT_API, true, MainActivity.this);
        }

        User user = User.instance();

        String serverIdStr = EncryptHelper.encrypt(String.valueOf(serverId));
        OkHttpUtils
                .post()
                .headers(requestHelper.BuildRequestHeader())
                .addParams("sid", serverIdStr)
                .addParams("device", Constant.DEVICE)
                .addParams("version", AppInfo.getVersionCode(MainActivity.this))
                .addParams("token", user.getToken())
                .url(requestUrl)
                .build()
                .execute(new ResponseHelper()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {}

                    @Override
                    public void onResponse(JSONObject result, int id) {}
                });
    }

    private class MyAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;
            while (i <= 100) {
                try {
                    Thread.sleep( durationDeterminate / 100);
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
            handleVpnStatusChange();
        }
    }

    private void initView(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userPhone = (TextView) findViewById(R.id.txtUserPhone);
        userType = (ImageView) findViewById(R.id.imgType);
        userExpireDate = (TextView) findViewById(R.id.txtUserExpireDate);
        userAvatar = (ImageView) findViewById(R.id.imgUserAvatar);
        serverListView = (ListView)findViewById(R.id.lstServers);
        serverListEmptyHint = (TextView)findViewById(R.id.txtEmptyHint);

        btnStart = (FloatingActionButton) findViewById(R.id.btnStart);
        btnStartProgress = findViewById(R.id.btnStartProgress);
        progressWave = findViewById(R.id.progressWave);
        progressWaveStop = findViewById(R.id.progressWaveStop);
        store = new SharedPreferencesHelper(getApplicationContext());
    }
}
