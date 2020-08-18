package com.slicejobs.algsdk.algtasklibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.slicejobs.algsdk.algtasklibrary.model.JsFileConfig;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.ServiceUtils;

import java.io.File;

public class JsDownloadService extends Service {

    private JsFileConfig[] jsFileConfigArray;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JsConfig jsConfig = (JsConfig) PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getObject(AppConfig.JS_CONFIG_OBJECT_KEY, JsConfig.class);
        if(jsConfig != null){
            jsFileConfigArray = jsConfig.getList();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceUtils.startForeground(this,1,"com.slicejobs.ailinggong.jsdownload","爱零工下载通知");
        }
        downloadIndexJsFile(this,0);
        return super.onStartCommand(intent, flags, startId);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int index = msg.arg1;
                downloadIndexJsFile(JsDownloadService.this, index + 1);
            }
        }
    };
    public void downloadIndexJsFile(Context context, int index){
        if(jsFileConfigArray != null && jsFileConfigArray.length != 0) {
            if (index < jsFileConfigArray.length) {
                JsFileConfig jsFileConfig = jsFileConfigArray[index];
                if (!FileUtil.fileIsExists(AppConfig.LOCAL_JS_DIR + File.separator + jsFileConfig.getFileName())) {
                    PRDownloader.initialize(context);
                    PRDownloader.download(jsFileConfig.getDownloadUrl(),
                            AppConfig.LOCAL_JS_DIR,
                            jsFileConfig.getFileName())
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
                                    Log.d("----------", "--JsDownloadService--onDownloadComplete--jsFileName--" + jsFileConfig.getFileName() + "---downloadUrl---" + jsFileConfig.getDownloadUrl());
                                    PRDownloader.shutDown();
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    msg.arg1 = index;
                                    handler.sendMessage(msg);
                                }

                                @Override
                                public void onError(Error error) {
                                    Log.d("----------", "--JsDownloadService--onError--jsFileName--" + jsFileConfig.getFileName() + "---downloadUrl---" + jsFileConfig.getDownloadUrl());
                                    PRDownloader.shutDown();
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    msg.arg1 = index;
                                    handler.sendMessage(msg);
                                }
                            });
                }
            } else {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }
}
