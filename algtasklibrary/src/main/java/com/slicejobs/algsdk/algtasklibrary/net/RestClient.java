package com.slicejobs.algsdk.algtasklibrary.net;

import android.app.slice.Slice;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.squareup.okhttp.OkHttpClient;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by nlmartian on 7/9/15.
 */
public class RestClient {
    public static final RestAdapter.LogLevel LOG_LEVEL =
            BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

    public static final int CONNECTION_TIMEOUT = 15_000;
    public static final int READ_TIMEOUT = 30_000;

    private static RestClient restClient;

    private static Object lock = new Object();

    public static RestClient getInstance() {
        if (restClient == null) {
            synchronized (lock){
                if (null == restClient) {
                    restClient = new RestClient();
                }
            }
        }

        return restClient;
    }



    private static RestAdapter restAdapter;
    private Api api;
    private String accessToken;

    private RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            String AppVersion = null;
            PackageInfo pInfo = null;
            try {
                pInfo = SliceApp.CONTEXT.getPackageManager().getPackageInfo(SliceApp.CONTEXT.getPackageName(), 0);
                AppVersion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String phoneVersion = android.os.Build.VERSION.RELEASE;
            String phoneModel = Build.MODEL;
            //获得设备号IMEI
            TelephonyManager telephonyManager = (TelephonyManager) SliceApp.CONTEXT.getSystemService(Context.TELEPHONY_SERVICE);

            String szImei = "0";
            try {
                szImei = telephonyManager.getDeviceId();
            } catch (SecurityException e) {//没有 android.permission.READ_PHONE_STATE

            }

                if (StringUtil.isNotBlank(accessToken)) {
                request.addHeader("SJ-Auth-Key", accessToken);
                request.addHeader("SJ-Client-Version", AppVersion);//app版本
                request.addHeader("SJ-Client-OSVersion", phoneVersion);//操作系统版本
                request.addHeader("SJ-Client-Model", Build.MANUFACTURER + "-" + phoneModel);//手机型号
                request.addHeader("SJ-Device-Number", szImei);//设备号
            } else if (StringUtil.isNotBlank(PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.AUTH_KEY, SliceStaticStr.NATIVE_TOKEN_ISNULL))) {
                request.addHeader("SJ-Auth-Key", PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.AUTH_KEY, SliceStaticStr.NATIVE_TOKEN_ISNULL));
                request.addHeader("SJ-Client-Version", AppVersion);//app版本
                request.addHeader("SJ-Client-OSVersion", phoneVersion);//操作系统版本
                request.addHeader("SJ-Client-Model", Build.MANUFACTURER + "-" + phoneModel);//手机型号
                request.addHeader("SJ-Device-Number", szImei);//设备号
            }
        }
    };

    private RestClient() {
        checkoutChannel();
    }

    private OkClient initClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClient.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        OkClient okClient = new OkClient(okHttpClient);
        return okClient;
    }

    public Api provideApi() {
        api = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.apiHost.getServerApiHost())
                .setRequestInterceptor(requestInterceptor)
                .setClient(initClient())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(LOG_LEVEL)
                .build();


        api = restAdapter.create(Api.class);
        return api;
    }

    public Api provideOpenApi() {
        api = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.apiHost.getOpenServerApiHost())
                .setRequestInterceptor(requestInterceptor)
                .setClient(initClient())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(LOG_LEVEL)
                .build();


        api = restAdapter.create(Api.class);
        return api;
    }

    public void setAccessToken(String token) {
        accessToken = token;
    }

    public void checkoutChannel() {
        api = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.apiHost.getServerApiHost())
                .setRequestInterceptor(requestInterceptor)
                .setClient(initClient())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(LOG_LEVEL)
                .build();


        api = restAdapter.create(Api.class);
    }


}
