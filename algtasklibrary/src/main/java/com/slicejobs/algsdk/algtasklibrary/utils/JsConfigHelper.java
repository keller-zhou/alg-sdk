package com.slicejobs.algsdk.algtasklibrary.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.JsConfig;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.taobao.weex.utils.WXFileUtils;

import java.io.File;

public class JsConfigHelper {

    public static void downLoadJsConfigJson(Context context){
        PRDownloader.initialize(context);
        PRDownloader.download(AppConfig.JS_PAGE_INFO_SERVER_DIR,
                AppConfig.LOCAL_JS_DIR,
                "page-info.json")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                })
                .start(new OnDownloadListener() {

                    @Override
                    public void onDownloadComplete() {

                        String jsConfigJson = WXFileUtils.loadFileOrAsset(AppConfig.LOCAL_JS_DIR + File.separator + "page-info.json",context);

                        if(StringUtil.isNotBlank(jsConfigJson)){
                            try {
                                JsConfig jsConfig = JSON.parseObject(jsConfigJson, JsConfig.class);
                                PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.JS_CONFIG_OBJECT_KEY,jsConfig);
                                int currCamera = PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
                                if(currCamera  == AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG
                                        || currCamera == AppConfig.SERVICE_PHOTO_CAMERA_SELECT_SYSTEM){//用户未在设置中手动设置过相机
                                    if(jsConfig != null){
                                        boolean isUseSystemCamera = jsConfig.isUseStytemCamera();
                                        if(isUseSystemCamera){
                                            PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_SYSTEM);
                                        }else {
                                            //不强制要求使用系统相机的，判断机型是否在黑名单内
                                            String cameraKitBlacklist = jsConfig.getCameraKitBlacklist();
                                            if(StringUtil.isNotBlank(cameraKitBlacklist)){
                                                if(cameraKitBlacklist.contains(Build.MODEL)){//机型在黑名单内
                                                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_SYSTEM);
                                                }else {
                                                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
                                                }
                                            }else {
                                                PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
                                            }

                                        }
                                    }
                                }
                                if(jsConfig != null){
                                    JsConfigHelper.startDownloadJsService(context);
                                }
                            } catch (JSONException e) {//配置文件解析异常

                            }

                        }
                    }

                    @Override
                    public void onError(Error error) {
                    }
                });
    }

    public static void startDownloadJsService(Context context){
        Intent downloadJsIntent = new Intent();
        downloadJsIntent.setAction("com.slicejobs.algsdk.algtasklibrary.JS_DOWNLOAD_SERVICE");
        downloadJsIntent.setPackage(context.getPackageName());
        context.startService(downloadJsIntent);
    }

    /*
    * 下载单个js文件
    * */
    public static void downloadJsFile(Context context, String jsFileName, String downloadUrl){
        PRDownloader.initialize(context);
        PRDownloader.download(downloadUrl,
                AppConfig.LOCAL_JS_DIR,
                jsFileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                })
                .start(new OnDownloadListener() {

                    @Override
                    public void onDownloadComplete() {
                        Log.d("----------", "--JsConfigHelper-onDownloadComplete--jsFileName--" + jsFileName + "---downloadUrl---" + downloadUrl);
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("----------", "--JsConfigHelper-onError----jsFileName--" + jsFileName + "---downloadUrl---" + downloadUrl);
                    }
                });
    }

}
