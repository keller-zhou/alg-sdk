package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hjq.toast.ToastUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.model.ApiHost;
import com.slicejobs.algsdk.algtasklibrary.model.JsAlertMsg;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.model.WebConfig;
import com.slicejobs.algsdk.algtasklibrary.model.WebHost;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.HallTaskActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.MapActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.MediaPlayerActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.RimTaskMapActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskPackageListActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskWebDetailActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.WebviewActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.WeexPublicActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.taobao.weex.appfram.storage.WXStorageModule;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;
import com.umeng.analytics.MobclickAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class WXBaseEventModule extends WXModule {

    //通知客户端更新用户信息
    @WXModuleAnno
    public void setUserInfo(String strJson) {//不能，否则json出异常Map<String,Object> params
        Gson mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        User user = mGson.fromJson(strJson, User.class);
        BizLogic.updateUser(user);
    }

    //通知客户端更新用户信息
    @WXModuleAnno
    public void setUserInfo(User user) {//不能，否则json出异常Map<String,Object> params
        BizLogic.updateUser(user);
    }

    @WXModuleAnno
    public void resetAccount() {
        SliceApp.resetAccount(mWXSDKInstance.getContext());
    }

    @WXModuleAnno
    public void getClientLocation(String no, String callbackId) {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果

        LocationClient locationClient = new LocationClient(mWXSDKInstance.getContext());
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                locationClient.unRegisterLocationListener(this);
                double lat = 0;
                double lon = 0;
                if (bdLocation != null && bdLocation.getLongitude() != 4.9E-324 && bdLocation.getLatitude() != 4.9E-324 && bdLocation.getLongitude() != 0 && bdLocation.getLatitude() != 0) {//获取到定位
                    lat = bdLocation.getLatitude();
                    lon = bdLocation.getLongitude();
                    PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putString(AppConfig.USER_LOCATION_KEY, lon + "," + lat);
                }

                //局部回调
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("lat", lat);
                result.put("lon", lon);
                result.put("addStr", bdLocation.getAddrStr());
                WXBridgeManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, result);
            }
        });
        locationClient.start();
    }

    @WXModuleAnno(runOnUIThread = true)
    public void printLog(String MSG) {
        Log.d("-----------------", "调试h5日志:" + MSG);
    }

    @WXModuleAnno
    public void umengCustomErrorLog(String msg) {
        MobclickAgent.reportError(SliceApp.CONTEXT, msg);
    }

    @WXModuleAnno
    public void openVideoPlayer(Map<String, Object> params) {
        if (params.get("src") != null) {
            String videoUrl = params.get("src").toString();
            mWXSDKInstance.getContext().startActivity(MediaPlayerActivity.getIntent(mWXSDKInstance.getContext(),videoUrl));
        }
    }

    //增加友盟点击，计数功能支持
    @WXModuleAnno
    public void umengCount(String eventId) {
        if (StringUtil.isNotBlank(eventId)) {
            MobclickAgent.onEvent(mWXSDKInstance.getContext(), eventId);
        }
    }

    @WXModuleAnno
    public void openWebview( Map<String,Object> params){//用户处理网页端想打开webview

        if (null != params.get("data")) {
            mWXSDKInstance.getContext().startActivity(WebviewActivity.getStartIntentCustom(mWXSDKInstance.getContext(), params.get("url").toString(), params));
        } else {
            mWXSDKInstance.getContext().startActivity(WebviewActivity.getStartIntent(mWXSDKInstance.getContext(), params.get("url").toString()));
        }
        Log.i("---","openWebview url="+params.get("url").toString());
    }

    @WXModuleAnno
    public void openModule(String modelName, Map<String, Object> params) {
        if (modelName.equals("周边赚")) {
            Intent intentMap =new Intent(mWXSDKInstance.getContext(),RimTaskMapActivity.class);
            mWXSDKInstance.getContext().startActivity(intentMap);
        } else if (StringUtil.isNotBlank(modelName)) {
            if (modelName.equals("地图")) {
                Double userLat = 0.0;
                Double userLon = 0.0;
                if(params != null) {
                    if (params.get("marketid") != null) {
                        JSONObject market = (JSONObject) params.get("marketinfo");
                        if(market != null){
                            mWXSDKInstance.getContext().startActivity(MapActivity.getMapActivityIntent(mWXSDKInstance.getContext(), userLon, userLat, params.get("marketid").toString(),market.get("longitude").toString(),market.get("latitude").toString()));
                        }
                    } else {
                        mWXSDKInstance.getContext().startActivity(MapActivity.getMapActivityIntent(mWXSDKInstance.getContext(), userLon, userLat));
                    }
                }else {
                    mWXSDKInstance.getContext().startActivity(MapActivity.getMapActivityIntent(mWXSDKInstance.getContext(), userLon, userLat));
                }
            } else if (modelName.equals("门店赚")) {
                Intent intentHall =new Intent(mWXSDKInstance.getContext(),HallTaskActivity.class);
                mWXSDKInstance.getContext().startActivity(intentHall);
            } else if (modelName.equals("openMoreTaskPackage")) {
                Intent intentTaskPackage =new Intent(mWXSDKInstance.getContext(),TaskPackageListActivity.class);
                mWXSDKInstance.getContext().startActivity(intentTaskPackage);
            }
        }
    }



    @WXModuleAnno
    public void openWeChat() {
        Intent lan = mWXSDKInstance.getContext().getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(lan.getComponent());
        mWXSDKInstance.getContext().startActivity(intent);
    }


    //打开公共界面
    @WXModuleAnno
    public void openPublicActivity(Map<String,Object> params) {
        Intent intent = new Intent(mWXSDKInstance.getContext(), WeexPublicActivity.class);
        Bundle bundle = new Bundle();
        SerializableBaseMap tmpmap=new SerializableBaseMap();
        tmpmap.setMap(params);
        bundle.putSerializable("weex_data", tmpmap);
        intent.putExtras(bundle);
        mWXSDKInstance.getContext().startActivity(intent);
    }

    //js对话框
    @WXModuleAnno
    public void showSJAlertView(String msg, String callbackId){
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        JsAlertMsg jsAlertMsg = gson.fromJson(msg, JsAlertMsg.class);
        String message = jsAlertMsg.getMessage();
        String defineText = jsAlertMsg.getOkTitle();
        String cancelText = jsAlertMsg.getCancelTitle();
        AlertDialog.Builder builer = new  AlertDialog.Builder(mWXSDKInstance.getContext(), R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(mWXSDKInstance.getContext());
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        tvTitle.setText(mWXSDKInstance.getContext().getString(R.string.text_slicejobs_hint));
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(message);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
        Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
        Button btDefine = (Button) view.findViewById(R.id.dialog_define);
        btCancel.setText(cancelText);
        btDefine.setText(defineText);
        tvBt.setText(defineText);
        if(cancelText != null && !cancelText.isEmpty()){
            tvBt.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }else{
            tvBt.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
        }
        builer.setCancelable(false);
        //builer.setView(view);
        AlertDialog dialog = builer.create();
        dialog.show();
        btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
            dialog.dismiss();
            //局部回调
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("buttonIndex", 0);
            WXBridgeManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, result);
        });
        btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
            dialog.dismiss();
            //局部回调
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("buttonIndex", 1);
            WXBridgeManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, result);
        });
        tvBt.setOnClickListener(v -> {
            dialog.dismiss();
            //局部回调
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("buttonIndex", 0);
            WXBridgeManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, result);
        });

        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager wm = (WindowManager) mWXSDKInstance.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (width * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    @WXModuleAnno
    public void showToast(String message){
        ToastUtils.show(message);
    }

    @WXModuleAnno
    public void logout() {//退出登录
        //删除基本配置
        PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, SliceStaticStr.INVALID_TOKEN);
        PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putString(AppConfig.PREF_USER, "");
        if (mWXSDKInstance.getContext() instanceof Activity) {
            Activity activity = (Activity) mWXSDKInstance.getContext();
            activity.finish();
        }
    }

    //打开web detail界面
    @WXModuleAnno
    public void openTaskWebDetail(Map<String, Object> params) {
        Intent intent = new Intent(mWXSDKInstance.getContext(), TaskWebDetailActivity.class);
        Bundle bundle = new Bundle();
        SerializableBaseMap tmpmap=new SerializableBaseMap();
        tmpmap.setMap(params);
        bundle.putSerializable("weex_data", tmpmap);
        intent.putExtras(bundle);

        mWXSDKInstance.getContext().startActivity(intent);
    }

    @WXModuleAnno
    public void popView() {
        if (mWXSDKInstance.getContext() instanceof Activity) {
            Activity activity = (Activity) mWXSDKInstance.getContext();
            activity.finish();
        }
    }



    public static void writeStorage(){
        WebConfig webConfig = new WebConfig();
        ApiHost apiHost = AppConfig.apiHost;
        WebHost webHost = AppConfig.webHost;
        webConfig.apiHost = apiHost;
        webConfig.webHost = webHost;
        webConfig.clientModel = Build.MANUFACTURER + "-" + Build.MODEL;
        //获得设备号IMEI
        TelephonyManager telephonyManager = (TelephonyManager) SliceApp.CONTEXT.getSystemService(Context.TELEPHONY_SERVICE);

        String szImei = "0";
        try {
            szImei = telephonyManager.getDeviceId();
        } catch (SecurityException e) {//没有 android.permission.READ_PHONE_STATE

        }
        webConfig.deviceNumber = szImei;

        String AppVersion = null;
        PackageInfo pInfo = null;
        try {
            pInfo = SliceApp.CONTEXT.getPackageManager().getPackageInfo(SliceApp.CONTEXT.getPackageName(), 0);
            AppVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        webConfig.version =  AppVersion;
        webConfig.env = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.IS_RELEASE, false)?"release":"debug";
        webConfig.phoneVersion = android.os.Build.VERSION.RELEASE;

        WXStorageModule storageModule = new WXStorageModule();
        Map<String,Object> appConfig = new HashMap<>();
        appConfig.put("user",BizLogic.getCurrentUser());
        appConfig.put("device",webConfig);
        appConfig.put("appId", PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID));
        Gson mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        String appConfigJson = mGson.toJson(appConfig);

        storageModule.setItem("user_global", appConfigJson, new JSCallback() {
            @Override
            public void invoke(Object data) {

            }

            @Override
            public void invokeAndKeepAlive(Object data) {

            }
        });
    }

    @WXModuleAnno
    public void unregisterXiaoMiPush() {

    }
}
