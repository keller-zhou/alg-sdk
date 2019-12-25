package com.slicejobs.algsdk.algtasklibrary.ui.base;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.JsConfig;
import com.slicejobs.algsdk.algtasklibrary.model.JsFileConfig;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.ImageAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.LoadingDialog;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.loading.LoadingAndRetryManager;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.loading.OnLoadingAndRetryListener;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.JsConfigHelper;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StatusBarUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;
import com.taobao.weex.utils.WXViewUtils;

import java.io.File;
import java.util.ArrayList;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by nlmartian on 7/8/15.
 */
@RuntimePermissions
public class BaseActivity extends SwipeBackActivity implements IWXRenderListener {
    public LoadingDialog progressDialog;
    WXSDKInstance mWXSDKInstance;
    LoadingAndRetryManager mLoadingAndRetryManager;
    private String jsFileName;
    private String jsonInitData;
    private String viewName;
    private IJsRenderListener iJsRenderListener;
    private boolean ifRenderFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式代码配置
        //当FitsSystemWindows设置 false 时，会在屏幕最上方预留出状态栏高度的 padding
        //当FitsSystemWindows设置 true 时 ，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        /*if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }*/
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        //设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityResume();
        }
    }

    public void onPause() {
        super.onPause();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showProgressDialog() {
        LoadingDialog.Builder loadBuilder=new LoadingDialog.Builder(this)
                .setShowMessage(false)
                .setCancelable(true)
                .setCancelOutside(true);
        if(progressDialog == null) {
            progressDialog = loadBuilder.create();
        }
        progressDialog.show();
    }

    public void showTextProgressDialog(String text) {
        LoadingDialog.Builder loadBuilder=new LoadingDialog.Builder(this)
                .setShowMessage(true)
                .setMessage(text)
                .setCancelable(false)
                .setCancelOutside(false);
        progressDialog=loadBuilder.create();
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public void toast(String msg){
        ToastUtils.show(msg);
    }

    public void umengCustomErrorLog(String msg) { //友盟收集自定义报错

    }

    /**
     * 适合简单提示
     * @param msgTitle
     * @param msg
     * @param posiText
     */
    public void showHintDialog(DialogDefineClick dialogDefineClick, String msgTitle, String msg, String posiText, boolean isCancellab) {
        AlertDialog.Builder builer = new  AlertDialog.Builder(this, R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(msg);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        tvBt.setText(posiText);
        if (StringUtil.isBlank(msgTitle)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(msgTitle);
        }
        //builer.setView(view);
        builer.setCancelable(isCancellab);
        AlertDialog dialog = builer.create();
        dialog.show();
        tvBt.setOnClickListener(v -> {
            dialog.dismiss();
            if (dialogDefineClick != null) {
                dialogDefineClick.defineClick();
            }
        });
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    /**
     *适合有确定按钮的提示对话框
     * @param msgTitle
     * @param msg
     * @param
     */
    public void showHintDialog(DialogClickLinear linear, String msgTitle, String msg, String cancelText, String defineText, boolean isCancellab) {

        AlertDialog.Builder builer = new  AlertDialog.Builder(this,R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(msg);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
            tvBt.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
        linearLayout.setVisibility(View.VISIBLE);
        Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
            btCancel.setText(cancelText);
        Button btDefine = (Button) view.findViewById(R.id.dialog_define);
            btDefine.setText(defineText);
        builer.setCancelable(isCancellab);
        if (StringUtil.isBlank(msgTitle)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(msgTitle);
        }
        //builer.setView(view);
        AlertDialog dialog = builer.create();
        dialog.show();
        btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.cancelClick();
            }
        });
        btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.defineClick();
            }
        });
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    /**
     *适合有三个按钮的提示对话框
     * @param msgTitle
     * @param msg
     * @param
     */
    public void showHintDialog(DialogThreeClickLinear linear, String msgTitle, String msg, String cancelText, String defineText, String middleText, boolean isCancellab) {

        AlertDialog.Builder builer = new  AlertDialog.Builder(this,R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(msg);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        tvBt.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_three_bt_layout);
        linearLayout.setVisibility(View.VISIBLE);
        Button btCancel = (Button) view.findViewById(R.id.bt_cancel);
        btCancel.setText(cancelText);
        Button btMiddle = (Button) view.findViewById(R.id.bt_middle);
        btMiddle.setText(middleText);
        Button btDefine = (Button) view.findViewById(R.id.bt_define);
        btDefine.setText(defineText);
        if (StringUtil.isBlank(msgTitle)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(msgTitle);
        }
        builer.setCancelable(isCancellab);
        //builer.setView(view);
        AlertDialog dialog = builer.create();
        dialog.show();
        btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.cancelClick();
            }
        });
        btMiddle.setOnClickListener(v -> {//点击中间按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.middleClick();
            }
        });
        btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.defineClick();
            }
        });
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }


    public interface  DialogClickLinear {

        public void cancelClick();

        public void defineClick();

    }

    public interface  DialogThreeClickLinear {

        public void cancelClick();
        public void middleClick();
        public void defineClick();

    }

    public interface  DialogDefineClick {
        public void defineClick();
    }

    public void addNetErrorView(OnNetErrorRefreshClick refreshClick){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.view_net_error, null);
        Button refresh = (Button) view.findViewById(R.id.operate);
        refresh.setOnClickListener(new View.OnClickListener() {//点击刷新重试按钮,调用接口
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                refreshClick.onRefreshClick();
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = DensityUtil.getStatusBarHeight(this) + DensityUtil.dip2px(this,43);
        this.addContentView(view,params);
    }

    public interface OnNetErrorRefreshClick{
        public void onRefreshClick();
    }


    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        if(iJsRenderListener != null){
            iJsRenderListener.onViewCreated(instance,view);
        }
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingAndRetryManager.showContent();
                if(ifRenderFromServer){//如果加载的是服务器上的js文件,下载下来
                    JsFileConfig jsFileConfig = getLocalJsFileConfig(jsFileName);
                    if(jsFileConfig != null){
                        JsConfigHelper.downloadJsFile(BaseActivity.this,jsFileName,jsFileConfig.getDownloadUrl());
                    }
                }
            }
        },200);
    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {

    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        if (StringUtil.isNotBlank(errCode) && errCode.equals("wx_network_error") || errCode.equals("-1002")) {//无网络原因
            mLoadingAndRetryManager.showRetry();
        } else {
            if (StringUtil.isNotBlank(errCode) && StringUtil.isNotBlank(msg)) {
                if (BizLogic.getCurrentUser() != null) {
                    umengCustomErrorLog(viewName + "页面报错userid=" + BizLogic.getCurrentUser().userid + "code:" + errCode + "msg:" + msg);
                }
            }
            Toast.makeText(this, "errCode:" + errCode + " Render ERROR:" + msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void renderJs(String jsFileName, String jsonInitData, String viewName, IJsRenderListener iJsRenderListener){
        //初始化h5模块
        InitConfig config = new InitConfig.Builder().setImgAdapter(new ImageAdapter()).build();
        try {
            WXSDKEngine.initialize(SliceApp.APPLICATION, config);
        } catch (Exception e) {

        }
        this.jsFileName = jsFileName;
        this.jsonInitData = jsonInitData;
        this.viewName = viewName;
        this.iJsRenderListener = iJsRenderListener;
        mWXSDKInstance = new WXSDKInstance(this);
        mWXSDKInstance.registerRenderListener(this);
        mLoadingAndRetryManager = LoadingAndRetryManager.generate(this, new OnLoadingAndRetryListener() {
            @Override
            public void setRetryEvent(View retryView) {
                View view = retryView.findViewById(R.id.operate);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadJs(mWXSDKInstance,jsFileName,jsonInitData);
                    }
                });
            }
        });
        BaseActivityPermissionsDispatcher.loadJsWithCheck(this,mWXSDKInstance,jsFileName,jsonInitData);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void loadJs(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        JsFileConfig jsFileConfig = getLocalJsFileConfig(jsFileName);
        if(jsFileConfig != null){//先判断本地配置json是否存在
            if(FileUtil.fileIsExists(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName)){//本地js文件存在
                String localJsMd5 = SignUtil.md5(WXFileUtils.loadFileOrAsset(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName,mWXSDKInstance.getContext()));
                if(localJsMd5.equals(jsFileConfig.getFileMD5())){//md5匹配用本地js
                    if(!jsFileConfig.isUseOnline){//不要求使用线上
                        loadJsFromLocal(mWXSDKInstance, jsFileName, jsonInitData);
                    }else {
                        loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
                    }
                }else {//md5不匹配用服务器js
                    FileUtil.deleteFile(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName);//md5不匹配删除本地js文件
                    ifRenderFromServer = true;
                    loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
                }
            }else {
                ifRenderFromServer = true;
                loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
            }
        }else {//本地文件不存在用服务器js
            ifRenderFromServer = true;
            loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
        }
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void onWriteReadDenied(){
        ifRenderFromServer = true;
        loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
    }

    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForWriteRead(PermissionRequest request){
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {
                ifRenderFromServer = true;
                loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivityForResult(intent,111);
            }
        }, "读取手机文件被禁止", "请在手机“设置-应用程序权限管理-选择本App”允许存储", "以后再说", "打开", false);
    }

    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void onWriteReadNeverAskAgain() {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {
                ifRenderFromServer = true;
                loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivityForResult(intent,111);
            }
        }, "读取手机文件被禁止", "请在手机“设置-应用程序权限管理-选择本App”允许存储", "以后再说", "打开", false);
    }

    private void loadJsFromServer(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        mWXSDKInstance.renderByUrl(this.getPackageName(), AppConfig.JS_SERVER_DIR + jsFileName, null, jsonInitData, WXViewUtils.getScreenWidth(this), WXViewUtils.getScreenHeight(this) - DensityUtil.dip2px(this, 56), WXRenderStrategy.APPEND_ASYNC);
    }

    private void loadJsFromLocal(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        mWXSDKInstance.render(
                this.getPackageName(),
                WXFileUtils.loadFileOrAsset(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName,this),
                null,
                jsonInitData,
                WXViewUtils.getScreenWidth(this),
                WXViewUtils.getScreenHeight(this) - DensityUtil.dip2px(this, 56),
                WXRenderStrategy.APPEND_ASYNC);
    }

    private JsFileConfig getLocalJsFileConfig(String jsFileName){
        JsConfig jsConfig = (JsConfig) PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getObject(AppConfig.JS_CONFIG_OBJECT_KEY, JsConfig.class);
        if(jsConfig != null){
            JsFileConfig[] jsFileConfigArray = jsConfig.getList();
            if(jsFileConfigArray != null && jsFileConfigArray.length != 0) {
                for (JsFileConfig jsFileConfig : jsFileConfigArray) {
                    if (jsFileConfig.getFileName().equals(jsFileName)) {
                        return jsFileConfig;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BaseActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111){
            BaseActivityPermissionsDispatcher.loadJsWithCheck(this,mWXSDKInstance,jsFileName,jsonInitData);
        }
    }
}


