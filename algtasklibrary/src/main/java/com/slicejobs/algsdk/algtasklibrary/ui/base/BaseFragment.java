package com.slicejobs.algsdk.algtasklibrary.ui.base;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
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

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by nlmartian on 7/11/15.
 */
@RuntimePermissions
public class BaseFragment extends Fragment implements IWXRenderListener {

    protected LoadingDialog progressDialog;
    protected FragmentActivity mActivity;
    WXSDKInstance mWXSDKInstance;
    LoadingAndRetryManager mLoadingAndRetryManager;
    private String jsFileName;
    private String jsonInitData;
    private String viewName;
    private IJsRenderListener iJsRenderListener;
    private boolean ifRenderFromServer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }

    public void showProgressDialog() {
        LoadingDialog.Builder loadBuilder=new LoadingDialog.Builder(mActivity)
                .setShowMessage(false)
                .setCancelable(true)
                .setCancelOutside(true);
        if (progressDialog == null) {
            progressDialog=loadBuilder.create();
        }
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


    public void showHintDialog(DialogDefineClick dialogDefineClick, String msgTitle, String msg, String posiText, boolean isCancellab) {
        try {
            AlertDialog.Builder builer = new AlertDialog.Builder(mActivity, R.style.Dialog_Fullscreen);
            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
            TextView tvTitle = (TextView) view.findViewById(R.id.dialog_title);
            TextView tvMsg = (TextView) view.findViewById(R.id.dialog_msg);
            tvMsg.setText(msg);
            TextView tvBt = (TextView) view.findViewById(R.id.dialog_bt_hint);
            tvBt.setText(posiText);
            if (StringUtil.isBlank(msgTitle) || msgTitle.equals(getResources().getString(R.string.text_slicejobs_hint))) {
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
            WindowManager m = getActivity().getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
            p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
            dialogWindow.setAttributes(p);
        }catch (Exception e){
            e.getStackTrace();
            umengCustomErrorLog("showHintDialog报错"  + e.getMessage());
        }
    }

    /**
     *适合有确定按钮的提示对话框
     * @param msgTitle
     * @param msg
     * @param
     */
    public void showHintDialog(DialogClickLinear linear, String msgTitle, String msg, String cancelText, String defineText, boolean isCancellab) {
        try{
            AlertDialog.Builder builer = new  AlertDialog.Builder(mActivity,R.style.Dialog_Fullscreen);
            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
            TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
            TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
            tvMsg.setText(msg);
            TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
            tvBt.setVisibility(View.GONE);
            if (StringUtil.isBlank(msgTitle) || msgTitle.equals(getResources().getString(R.string.text_slicejobs_hint))) {
                tvTitle.setVisibility(View.GONE);
            } else {
                tvTitle.setVisibility(View.VISIBLE);
                tvTitle.setText(msgTitle);
            }
            LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
            linearLayout.setVisibility(View.VISIBLE);
            Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
            btCancel.setText(cancelText);
            Button btDefine = (Button) view.findViewById(R.id.dialog_define);
            btDefine.setText(defineText);
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
            btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
                dialog.dismiss();
                if (linear != null) {
                    linear.defineClick();
                }
            });
            Window dialogWindow = dialog.getWindow();
            dialogWindow.setContentView(view);
            dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager m = getActivity().getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
            p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
            dialogWindow.setAttributes(p);
        }catch (Exception e){
            e.getStackTrace();
            umengCustomErrorLog("showHintDialog报错"  + e.getMessage());
        }
    }



    public interface  DialogClickLinear {

        public void cancelClick();

        public void defineClick();

    }

    public interface  DialogDefineClick {
        public void defineClick();
    }

    public void umengCustomErrorLog(String msg) { //友盟收集自定义报错
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
                        JsConfigHelper.downloadJsFile(getActivity(),jsFileName,jsFileConfig.getDownloadUrl());
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
            ToastUtils.show("errCode:" + errCode + " Render ERROR:" + msg);
        }
    }

    public void renderJs(View view, String jsFileName, String jsonInitData, String viewName, IJsRenderListener iJsRenderListener){
        //初始化h5模块
        SliceApp.getInstance().initWeex();
        this.jsFileName = jsFileName;
        this.viewName = viewName;
        this.jsonInitData = jsonInitData;
        this.viewName = viewName;
        this.iJsRenderListener = iJsRenderListener;
        mWXSDKInstance = new WXSDKInstance(getActivity());
        mWXSDKInstance.registerRenderListener(this);
        mLoadingAndRetryManager = LoadingAndRetryManager.generate(view, new OnLoadingAndRetryListener() {
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
        BaseFragmentPermissionsDispatcher.loadJsWithCheck(this,mWXSDKInstance,jsFileName,jsonInitData);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void loadJs(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        JsFileConfig jsFileConfig = getLocalJsFileConfig(jsFileName);
        if(jsFileConfig != null){//先判断本地配置json是否存在
            if(FileUtil.fileIsExists(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName)){//本地js文件存在
                String localJsMd5 = SignUtil.md5(WXFileUtils.loadFileOrAsset(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName,mWXSDKInstance.getContext()));
                if(localJsMd5.equals(jsFileConfig.getFileMD5())){//md5匹配用本地js
                    if(!jsFileConfig.isUseOnline) {//不要求使用线上
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
        showHintDialog(new BaseFragment.DialogClickLinear() {
            @Override
            public void cancelClick() {
                ifRenderFromServer = true;
                loadJsFromServer(mWXSDKInstance,jsFileName,jsonInitData);
            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivityForResult(intent,222);
            }
        }, "读取手机文件被禁止", "请在手机“设置-应用程序权限管理-选择本App”允许存储", "以后再说", "打开", false);
    }

    private void loadJsFromServer(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        mWXSDKInstance.renderByUrl(mActivity.getPackageName(), AppConfig.JS_SERVER_DIR + jsFileName, null, jsonInitData, WXViewUtils.getScreenWidth(mActivity), WXViewUtils.getScreenHeight(mActivity) - DensityUtil.dip2px(mActivity, 56), WXRenderStrategy.APPEND_ASYNC);
    }

    private void loadJsFromLocal(WXSDKInstance mWXSDKInstance, String jsFileName, String jsonInitData){
        mWXSDKInstance.render(
                mActivity.getPackageName(),
                WXFileUtils.loadFileOrAsset(AppConfig.LOCAL_JS_DIR + File.separator + jsFileName,mActivity),
                null,
                jsonInitData,
                WXViewUtils.getScreenWidth(mActivity),
                WXViewUtils.getScreenHeight(mActivity) - DensityUtil.dip2px(mActivity, 56),
                WXRenderStrategy.APPEND_ASYNC);
    }

    private JsFileConfig getLocalJsFileConfig(String jsFileName){
        JsConfig jsConfig = (JsConfig) PrefUtil.make(getActivity(), PrefUtil.PREFERENCE_NAME).getObject(AppConfig.JS_CONFIG_OBJECT_KEY, JsConfig.class);
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
        BaseFragmentPermissionsDispatcher.onRequestPermissionsResult(this,requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 222){
            BaseFragmentPermissionsDispatcher.loadJsWithCheck(this,mWXSDKInstance,jsFileName,jsonInitData);
        }
    }
}
