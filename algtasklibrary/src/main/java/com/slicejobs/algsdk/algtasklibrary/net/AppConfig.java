package com.slicejobs.algsdk.algtasklibrary.net;


import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.model.ApiHost;
import com.slicejobs.algsdk.algtasklibrary.model.WebHost;
import com.slicejobs.algsdk.algtasklibrary.utils.VersionUtil;

/**
 * Created by nlmartian on 7/9/15.
 */
public class AppConfig {


    public static ApiHost apiHost = new ApiHost();


    public static WebHost webHost = new WebHost();


    public static final int VCODE_WAIT_TIME = BuildConfig.DEBUG ? 25 : 60;

    // app preferences
    public static final String PREF_USER = "user";
    public static final String AUTH_KEY = "auth_key";
    public static final String CURRENT_CITY = "current_city";
    public static final String USER_MARKET = "user_market";
    public static final String ACTIVITIES_INFO = "activities_info";

    // activity preferences
    public static final String ACT_PREF = "activity_preference";


    public static final String APP_FOLDER_NAME = "AlgSDK";

    public static final String WX_APP_ID = "wx3ec2d279bea306bf";

    public static final String CAMERA_TYPE = "cameraType";//存放使用相机类型


    public static final String UPLOAD_TYPE = "uploadType";//任务上传(立即上传和缓存上传)

    public static String TEMP_CACHE_DIR = null;

    public static String LONG_CACHE_DIR = null;

    public static String LOCAL_JS_DIR = null;


    public static final String IS_MD5_OSSVERIFI = "ossverifi";

    public static final String IS_IGNORE_CURRESS_VERSION = "ignoreVersion";//所忽略更新的版本

    public static final String DOWMLOAD_APP_SIGN = "downloadApk";//保存下载完成后，当前apk的签名，

    public static final String APP_CACAHE_DIRNAME = "/webcache";//webView缓存目录

    public static final String WEBVIEW_CACHE_CANCEL = "cache_cancel";



    public static String JS_SERVER_DIR = webHost.getAppWebHost() + "/private_sdk/build1.0.0/modules/";
    public static String JS_PAGE_INFO_SERVER_DIR = webHost.getAppWebHost() + "/private_sdk/build1.0.0/page-info.json";


    //weex
    public static final String HOME_VIEW_FILE = "index.js";


    public static final String MODIFY_PASSWORD_VIEW_FILE = "modify-pwd.js";

    //周边赚
    public static final String TASK_LIST_NEARBY_VIEW_FILE = "nearby-tasklist.js";

    //任务详情
    public static final String TASK_DETAIL_VIEW_FILE = "task-detail.js";

    //分布任务界面
    public static final String TASK_STEPS_VIEW_FILE = "task-steps.js";

    //门店信息收集分布任务界面
    public static final String MARKET_GATHER_TASK_STEPS_VIEW_FILE = "market-gather-task-steps.js";

    //门店任务
    public static final String TASK_LIST_STORE_VIEW_FILE = "market-tasklist.js";

    //关注门店
    public static final String MODIFY_MARKET_VIEW_FILE = "modify-market.js";

    //保存user位置
    public static final String USER_LOCATION_KEY = "userLocation";

    //是否能正确找到相机程序
    public static final String USER_STYTEM_CAMERA__KEY = "stytemCamera";


    //用于保存阿里云上传token(短期内只用一个token)
    public static final String OSS_TOKEN_KEY = "oss_token_key";

    //用于保存上次登录的日期
    public static final String LAST_LOGIN_APP_DATE = "last_login_app_date";

    //是否第一次登录社区
    public static final String IF_FIRST_LOGIN_MOBBBS = "if_first_login_mobbbs";


    public static final String VIDEO_CAMERA_TYPE = "videoCameraType";//存放视频使用相机类型

    public static final String JS_CONFIG_OBJECT_KEY = "save-js-cofig-object-key";

    //任务包界面
    public static final String TASK_PACKAGE_DETAIL = "taskpackage-detail.js";
    //任务包界面
    public static final String TASK_PACKAGE_LIST = "taskpackage-list.js";

    //当前位置相关信息
    public static final String USER_LOCATION_INFO = "user_location_info";

    //当前选择拍照相机
    public static final int SERVICE_PHOTO_CAMERA_SELECT_ALG = 1;
    public static final int SERVICE_PHOTO_CAMERA_SELECT_SYSTEM = 2;
    public static final int LOCAL_PHOTO_CAMERA_SELECT_ALG = 3;
    public static final int LOCAL_PHOTO_CAMERA_SELECT_SYSTEM = 4;

    //当前选择视频相机
    public static final int SERVICE_VIDEO_CAMERA_SELECT_ALG = 1;
    public static final int SERVICE_VIDEO_CAMERA_SELECT_SYSTEM = 2;


}
