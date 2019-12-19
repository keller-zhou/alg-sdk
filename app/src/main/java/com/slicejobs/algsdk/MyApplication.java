package com.slicejobs.algsdk;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SliceApp.getInstance().init(this,false);
    }
}
