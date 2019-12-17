package com.slicejobs.algsdk.algtasklibrary.model;

import com.slicejobs.algsdk.algtasklibrary.BuildConfig;

import java.io.Serializable;

public class ApiHost implements Serializable {


    //server api 环境
    public static final String BASE_HTTPS_SERVER_DEBUG = "https://api2-ci.slicejobs.com";

    public static final String BASE_HTTPS_SERVER_RELEASE = "https://api2.slicejobs.com";


    private String serverApiHost = "release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTPS_SERVER_RELEASE : BASE_HTTPS_SERVER_DEBUG;



    public String getServerApiHost() {
        return serverApiHost;
    }

    public void setServerApiHost(String serverApiHost) {
        this.serverApiHost = serverApiHost;
    }


    public ApiHost(){}

}
