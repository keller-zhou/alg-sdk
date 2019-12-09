package com.slicejobs.algsdk.algtasklibrary.app;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastAliPayStyle;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.LoginActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.MainActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.ImageAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.AudioPlayerComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.GifComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.HorizontalListComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.RecyclerComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.WebViewRichTextComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent.WebViewRichTitleComponent;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXBaseEventModule;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXHomeEventModule;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXTaskEventModule;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXUserCanterEventModule;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.JsConfigHelper;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;

import java.io.File;
import java.util.List;

/**
 * Created by nlmartian on 7/8/15.
 */
public class SliceApp{

    private static SliceApp instance = new SliceApp();
    public static Context CONTEXT;
    private Application mApplication;

    public synchronized static SliceApp getInstance() {
        return instance;
    }

    public void init (Application application) {
        this.mApplication = application;
        CONTEXT = application.getApplicationContext();
        mainProcessInit();
    }

    public void openAlg () {
        if (CONTEXT == null) {
            return;
        }
        Intent intent = null;
        if (isLogin()) {
            intent = new Intent(CONTEXT, MainActivity.class);
        } else {
            intent = new Intent(CONTEXT, LoginActivity.class);
        }
        CONTEXT.startActivity(intent);
        WXBaseEventModule.writeStorage();
    }

    private boolean isLogin() {
        User user = BizLogic.getCurrentUser();
        String authKey = PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.AUTH_KEY, SliceStaticStr.NATIVE_TOKEN_ISNULL);//当key不存在返回NATIVE_TOKEN_ISNULL
        if (StringUtil.isBlank(authKey) || authKey.equals(SliceStaticStr.NATIVE_TOKEN_ISNULL) || authKey.equals(SliceStaticStr.INVALID_TOKEN)) {//用户验证为空
            return false;
        } else if (user == null || null == user.userid) {//用户为空
            return false;
        }
        return true;
    }

    private void mainProcessInit() {

        initImageLoader(CONTEXT);

        // 设置AuthKey
        String authKey = PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.AUTH_KEY, SliceStaticStr.NATIVE_TOKEN_ISNULL);
        RestClient.getInstance().setAccessToken(authKey);

        try {
            initDir();
        } catch (NullPointerException e) {

        }

        //初始化百度地图
        SDKInitializer.initialize(CONTEXT);


        //初始化h5模块
        InitConfig config = new InitConfig.Builder().setImgAdapter(new ImageAdapter()).build();
        try {
            WXSDKEngine.initialize(mApplication, config);
        } catch (Exception e) {

        }

        try {
            //注册扩展
            WXSDKEngine.registerComponent("evidence-list", RecyclerComponent.class);
            WXSDKEngine.registerComponent("example-list", HorizontalListComponent.class);
            WXSDKEngine.registerComponent("native-webview", WebViewRichTextComponent.class);
            WXSDKEngine.registerComponent("native-webview-title", WebViewRichTitleComponent.class);
            WXSDKEngine.registerComponent("record-player", AudioPlayerComponent.class);
            WXSDKEngine.registerComponent("gif-imageview", GifComponent.class);
            WXSDKEngine.registerModule("native_base_event", WXBaseEventModule.class);
            WXSDKEngine.registerModule("task_event", WXTaskEventModule.class);
            WXSDKEngine.registerModule("home_event", WXHomeEventModule.class);
            WXSDKEngine.registerModule("user_center_event", WXUserCanterEventModule.class);
        } catch (WXException e) {
            Log.d("----------------", "启动出现异常"+e);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        ToastUtils.init(mApplication, new ToastAliPayStyle(CONTEXT));
        JsConfigHelper.downLoadJsConfigJson(CONTEXT);
    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.memoryCache(new WeakMemoryCache());
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());
    }

    private void initDir() throws NullPointerException {
        if (Environment.isExternalStorageEmulated()) {
            File extStore = Environment.getExternalStorageDirectory();
            File appDir = new File(extStore.getAbsolutePath() + "/" + AppConfig.APP_FOLDER_NAME);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
        }

        //小米应用市场测试机，SD卡状态虽然存在，但是可能是不可写的,CONTEXT.getExternalFilesDir(null)返回文件根本不存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && null != CONTEXT.getExternalFilesDir(null)) {

            File webCache = new File(CONTEXT.getExternalFilesDir(null).getAbsolutePath() + AppConfig.APP_CACAHE_DIRNAME);
            if (!webCache.exists()) {
                webCache.mkdir();
            }
            AppConfig.TEMP_CACHE_DIR = CONTEXT.getExternalFilesDir(null).getAbsolutePath() + File.separator + "currTaskCache";//立即上传，临时目录
            AppConfig.LONG_CACHE_DIR = CONTEXT.getExternalFilesDir(null).getPath() + File.separator + "cacheTask";//缓存上传，长时目录
            AppConfig.LOCAL_JS_DIR = CONTEXT.getExternalFilesDir(null).getPath() + File.separator + "localJs";//本地js文件存放目录
            FileUtil.createDirIfNotExisted(AppConfig.LOCAL_JS_DIR,false);
            FileUtil.createDirIfNotExisted(AppConfig.TEMP_CACHE_DIR,false);
        }
    }


    public static void resetAccount() {
        PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, SliceStaticStr.INVALID_TOKEN);
    }

}
