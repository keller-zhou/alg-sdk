package com.slicejobs.algsdk.algtasklibrary.model;

import com.slicejobs.algsdk.algtasklibrary.BuildConfig;

import java.io.Serializable;

public class WebHost implements Serializable {

    public WebHost(){}

    public WebHost(String env) {
        if (env.equals("release")) {
            appWebHost = BASE_HTTP_APP_WEB_RELEASE;
            shopWebHost = BASE_HTTP_SHOP_WEB_RELEASE;
        } else if (env.equals("ci")) {
            appWebHost = BASE_HTTP_APP_WEB_CI;
            shopWebHost = BASE_HTTP_SHOP_WEB_CI;
        } else if (env.equals("dev")) {
            appWebHost = BASE_HTTPS_APP_WEB_DEV;
            shopWebHost = BASE_HTTP_SHOP_WEB_DEV;
        } else {
            appWebHost = BASE_HTTP_APP_WEB_DEBUG;
            shopWebHost = BASE_HTTP_SHOP_WEB_DEBUG;
        }
    }



    //app web url
    public static final String BASE_HTTP_APP_WEB_DEBUG = "http://mcdn-debug.slicejobs.com";

    public static final String BASE_HTTP_APP_WEB_CI = "https://app-site-ci.slicejobs.com";

    public static final String BASE_HTTPS_APP_WEB_DEV = "https://app-site-dev.slicejobs.com";//3.2.2以前:https://mobile-dev.slicejobs.com

    public static final String BASE_HTTP_APP_WEB_RELEASE = "https://app.slicejobs.com";//3.2.2以前：https://mobile.slicejobs.com

    private String appWebHost ="release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_APP_WEB_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_APP_WEB_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTPS_APP_WEB_DEV : BASE_HTTP_APP_WEB_DEBUG));

    //shop web url
    public static final String BASE_HTTP_SHOP_WEB_DEBUG = "http://shop-debug.slicejobs.com";

    public static final String BASE_HTTP_SHOP_WEB_CI = "https://shop-ci.slicejobs.com";

    public static final String BASE_HTTP_SHOP_WEB_DEV= "https://shop-dev.slicejobs.com";//3.2.2以前:https://mobile-dev.slicejobs.com

    public static final String BASE_HTTP_SHOP_WEB_RELEASE = "https://shop.slicejobs.com";//3.2.2以前：https://mobile.slicejobs.com

    private String shopWebHost ="release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_SHOP_WEB_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_SHOP_WEB_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTP_SHOP_WEB_DEV : BASE_HTTP_SHOP_WEB_DEBUG));

    public String getAppWebHost() {
        return appWebHost;
    }

    public void setAppWebHost(String appWebHost) {
        this.appWebHost = appWebHost;
    }

    public String getShopWebHost() {
        return shopWebHost;
    }

    public void setShopWebHost(String shopWebHost) {
        this.shopWebHost = shopWebHost;
    }
}
