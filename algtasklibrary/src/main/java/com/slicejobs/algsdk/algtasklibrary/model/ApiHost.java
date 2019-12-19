package com.slicejobs.algsdk.algtasklibrary.model;

import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;

import java.io.Serializable;

public class ApiHost implements Serializable {


    //server api 环境
    public static final String BASE_HTTPS_SERVER_DEBUG = "http://api2-dev.slicejobs.com";

    public static final String BASE_HTTPS_SERVER_RELEASE = "https://api2.slicejobs.com";


    private String serverApiHost = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.IS_RELEASE, false)
            ? BASE_HTTPS_SERVER_RELEASE : BASE_HTTPS_SERVER_DEBUG;



    public String getServerApiHost() {
        return serverApiHost;
    }

    public void setServerApiHost(String serverApiHost) {
        this.serverApiHost = serverApiHost;
    }

    //server api 环境
    public static final String BASE_HTTPS_OPEN_SERVER_DEBUG = "http://openapi-dev.slicejobs.com";

    public static final String BASE_HTTPS_OPEN_SERVER_RELEASE = "https://openapi.slicejobs.com";


    private String openServerApiHost = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.IS_RELEASE, false)
            ? BASE_HTTPS_OPEN_SERVER_RELEASE : BASE_HTTPS_OPEN_SERVER_DEBUG;


    public String getOpenServerApiHost() {
        return openServerApiHost;
    }

    public void setOpenServerApiHost(String openServerApiHost) {
        this.openServerApiHost = openServerApiHost;
    }

    public ApiHost(){}

}
