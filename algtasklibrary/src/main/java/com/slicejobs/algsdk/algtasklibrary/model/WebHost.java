package com.slicejobs.algsdk.algtasklibrary.model;

import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;

import java.io.Serializable;

public class WebHost implements Serializable {

    public WebHost(){}

    //app web url
    public static final String BASE_HTTP_APP_WEB_DEBUG = "https://app-site-ci.slicejobs.com";

    public static final String BASE_HTTP_APP_WEB_RELEASE = "https://app.slicejobs.com";//3.2.2以前：https://mobile.slicejobs.com

    private String appWebHost = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.IS_RELEASE, false)
            ? BASE_HTTP_APP_WEB_RELEASE : BASE_HTTP_APP_WEB_DEBUG;


    public String getAppWebHost() {
        return appWebHost;
    }

    public void setAppWebHost(String appWebHost) {
        this.appWebHost = appWebHost;
    }
}
