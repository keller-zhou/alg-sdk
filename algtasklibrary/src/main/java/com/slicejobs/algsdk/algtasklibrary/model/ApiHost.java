package com.slicejobs.algsdk.algtasklibrary.model;

import com.slicejobs.algsdk.algtasklibrary.BuildConfig;

import java.io.Serializable;

public class ApiHost implements Serializable {


    //server api 环境
    public static final String BASE_HTTP_SERVER_DEBUG = "http://api2-dev.slicejobs.com";

    public static final String BASE_HTTP_SERVER_DEV = "https://api2-dev.slicejobs.com";

    public static final String BASE_HTTP_SERVER_CI = "http://api2-ci.slicejobs.com";

    public static final String BASE_HTTPS_SERVER_CI = "https://api2-ci.slicejobs.com";

    public static final String BASE_HTTPS_SERVER_RELEASE = "https://api2.slicejobs.com";


    private String serverApiHost = "release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTPS_SERVER_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_SERVER_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTP_SERVER_DEV : BASE_HTTP_SERVER_DEBUG));



    public String getServerApiHost() {
        return serverApiHost;
    }

    public void setServerApiHost(String serverApiHost) {
        this.serverApiHost = serverApiHost;
    }

    //商城 api
    public static final String BASE_HTTP_SHOP_DEBUG = "http://shop-api-dev.slicejobs.com/api";

    public static final String BASE_HTTP_SHOP_CI = "http://shop-api-ci.slicejobs.com/api";

    public static final String BASE_HTTP_SHOP_DEV = "http://shop-api-dev.slicejobs.com/api";

    public static final String BASE_HTTPS_SHOP_RELEASE = "https://shop-api.slicejobs.com/api";





    private String shopApiHost = "release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTPS_SHOP_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_SHOP_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTP_SHOP_DEV : BASE_HTTP_SHOP_DEBUG));


    public String getShopApiHost() {
        return shopApiHost;
    }

    public void setShopApiHost(String shopApiHost) {
        this.shopApiHost = shopApiHost;
    }



    //招聘 api
    public static final String BASE_HTTP_JOB_DEBUG = "http://recruit-dev.slicejobs.com/api";

    public static final String BASE_HTTP_JOB_CI = "http://recruit-ci.slicejobs.com/api";

    public static final String BASE_HTTP_JOB_DEV = "http://recruit-dev.slicejobs.com/api";

    public static final String BASE_HTTPS_JOB_RELEASE = "https://recruit-api.slicejobs.com/api";

    private String jobApiHost =  "release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTPS_JOB_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_JOB_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTP_JOB_DEV : BASE_HTTP_JOB_DEBUG));



    public String getJobApiHost() {
        return jobApiHost;
    }

    public void setJobApiHost(String jobApiHost) {
        this.jobApiHost = jobApiHost;
    }


    //扫码 api（理论废弃）
    public static final String BASE_HTTP_BARCODE_DEBUG = "http://barcode-ci.slicejobs.com/userapi";

    public static final String BASE_HTTP_BARCODE_CI = "http://barcode-ci.slicejobs.com/userapi";

    public static final String BASE_HTTP_BARCODE_DEV = "http://barcode-ci.slicejobs.com/userapi";

    public static final String BASE_HTTP_BARCODE_RELEASE = "http://barcode.slicejobs.com/userapi";

    private String barcodeApiHost =  "release".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_BARCODE_RELEASE : ("ci".equals(BuildConfig.BUILD_TYPE)
            ? BASE_HTTP_BARCODE_CI :("dev".equals(BuildConfig.BUILD_TYPE)
            ?BASE_HTTP_BARCODE_DEV : BASE_HTTP_BARCODE_DEBUG));


    public ApiHost(){}

    public ApiHost(String env) {
        if (env.equals("release")) {
            serverApiHost = BASE_HTTPS_SERVER_RELEASE;
            shopApiHost = BASE_HTTPS_SHOP_RELEASE;
            jobApiHost = BASE_HTTPS_JOB_RELEASE;
            barcodeApiHost = BASE_HTTP_BARCODE_RELEASE;
        } else if (env.equals("ci")) {
            serverApiHost = BASE_HTTPS_SERVER_CI;
            shopApiHost = BASE_HTTP_SHOP_CI;
            jobApiHost = BASE_HTTP_JOB_CI;
            barcodeApiHost = BASE_HTTP_BARCODE_CI;
        } else if (env.equals("dev")) {
            serverApiHost = BASE_HTTP_SERVER_DEV;
            shopApiHost = BASE_HTTP_SHOP_DEV;
            jobApiHost = BASE_HTTP_JOB_DEV;
            barcodeApiHost = BASE_HTTP_BARCODE_DEV;
        } else {
            serverApiHost = BASE_HTTP_SERVER_CI;
            shopApiHost = BASE_HTTP_SHOP_DEBUG;
            jobApiHost = BASE_HTTP_JOB_DEBUG;
            barcodeApiHost = BASE_HTTP_BARCODE_DEBUG;
        }
    }

}
