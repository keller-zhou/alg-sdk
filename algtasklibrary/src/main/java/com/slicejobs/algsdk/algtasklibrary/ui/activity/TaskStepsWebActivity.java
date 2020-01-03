package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.listener.ScreenListener;
import com.slicejobs.algsdk.algtasklibrary.model.EvidenceRequest;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTask;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.Photo;
import com.slicejobs.algsdk.algtasklibrary.model.PhotoRequirement;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepParams;
import com.slicejobs.algsdk.algtasklibrary.model.TempUploadTask;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.net.UploadTaskStepResultTask;
import com.slicejobs.algsdk.algtasklibrary.service.FloatingRecordService;
import com.slicejobs.algsdk.algtasklibrary.service.TaskResultUploadService;
import com.slicejobs.algsdk.algtasklibrary.ui.base.PickPhotoActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.ALGRecordFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.TaskUploadProgressFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.UploadPhotoDialogFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ActionSheetDialog;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.LoadingDialog;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.NetWorkUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by keller.zhou on 17/3/3.
 */
@RuntimePermissions
public class TaskStepsWebActivity extends PickPhotoActivity implements IJsRenderListener,UploadPhotoDialogFragment.OnTakePhotoListener,ALGRecordFragment.AlgRecordFinishListener,TaskUploadProgressFragment.TaskUploadEventListener {

    @BindView(R2.id.rootView)
    FrameLayout rootView;
    @BindView(R2.id.tasksteps_view)
    RelativeLayout taskStepsView;
    @BindView(R2.id.indicator_upload_status)
    FrameLayout uploadStatusIndicator;
    WXSDKInstance mWXSDKInstance;

    private static final String SYSTEM_REASON = "reason";
    private static final String SYSTEM_HOME_KEY = "homekey";
    public static final String CURRENT_RECORD_TIME_KEY = "currentRecordTime";
    public static final String CURRENT_RECORD_STATUS_KEY = "currentRecordStatus";
    public static final int BROWER_IMAGE_REQUEST_CODE = 10000;
    public static final int BROWER_CACHE_IMAGE_REQUEST_CODE = 10001;
    public static HashMap<String,String> UPLOADJSONMAP = new HashMap<>();
    public static final String TEMP_UPLOAD_MINITASK_KEY = "temp_upload_mini_task_key";
    public static final String TEMP_UPLOAD_CACHEDIR_KEY = "temp_upload_cachedir_key";
    public static final String TEMP_UPLOAD_STATUS_KEY = "temp_upload_status_key";
    public static final String TEMP_UPLOAD_ISCOMMIT_KEY = "temp_upload_iscommit_key";
    public static final String TEMP_UPLOAD_STEP_INDEX_KEY = "temp_upload_step_index_key";
    public static final String TEMP_UPLOAD_HASHMAP_JSON_KEY = "temp_upload_hashmap_key_key";
    private String cacheDir;

    private Task task;

    public static final int CODE_UPLOAD_REQUEST = 10201;

    private EvidenceRequest evidenceRequest = new EvidenceRequest();
    private boolean forceCamera = true;
    private boolean allowReusePhoto;
    private StringBuilder initJsonBuild;
    public static final String CACHE_SPACE = "s1l2i3c4e5j6o7b8s9";
    public static final String CACHE_FILE_NAME = "SlicejbosNativeCachePackage.txt";
    public static final int CACHE_RESULT_GOBACK = 31;
    private ArrayList<String> panoPhotos = new ArrayList<>();
    private LocationClient locationClient = null;
    private BDAbstractLocationListener mBDLocationListener;
    private double latitude;
    private double longitude;
    private String resultData;
    private int takePhotoAuxiliaryLine;
    private FragmentManager fragmentManager;
    private ALGRecordFragment algRecordFragment;
    private boolean isRecorder = false;//是否证在录音
    private boolean isRecorderEnd = false;//是否录音完成
    private boolean isRecorderFloating = false;//是否录音悬浮
    private ScreenListener screenListener;
    private boolean ifHomePressed;
    private UploadTaskStepResultTask uploadTaskStepResultTask;
    private boolean ifNotifyedFlag;//每一步缓存上传完毕后是否通知了js,通知过之后为true，防止一步发多次通知造成步骤链错误
    private Gson mGson;
    private int currentStep = 1;
    private Intent tempUploadIntent;
    private HomeKeyReceiver homeKeyReceiver;
    private Intent floatRecordIntent;
    private String stepId;
    private boolean isCommit;
    private MiniTask tempUploadMiniTask;
    private boolean allowCrossSubjectRecord;
    private TaskUploadProgressFragment taskUploadProgressFragment;
    private String stepIndex = "第1题";
    private ArrayList<String> uploadedStepIndexs;
    private ArrayList<String> waitStepIndexs;
    private int uploadingProgress = 101;
    private MultiUploadReceiver multiLineReceiver;
    private boolean isShowToCacheViewDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_tasksteps_web);
        setSwipeBackEnable(false);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        Bundle bundle = getIntent().getExtras();
        SerializableBaseMap serializableBaseMap = (SerializableBaseMap) bundle.get("weex_data");
        Map<String, Object> data = serializableBaseMap.getMap();
        mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        TaskStepParams taskStepParams  =  mGson.fromJson(data.get("initData").toString(), TaskStepParams.class);
        task = taskStepParams.getTask();

        //创建目录
        if (StringUtil.isNotBlank(task.getOrderid())) {
            String fileDirPath = AppConfig.TEMP_CACHE_DIR + File.separator + BizLogic.getCurrentUser().userid + "-" + task.getOrderid();
            if (FileUtil.createDirIfNotExisted(fileDirPath, false)) {
                FileUtil.createDirIfNotExisted(fileDirPath + File.separator + "Thumbnail", false);//创建图片缩略图目录
                FileUtil.createDirIfNotExisted(fileDirPath + File.separator + "Photo", false);//创建图片路径
            }
            cacheDir = fileDirPath;
        }



        int currCamera = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
        if (currCamera == AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG
                || currCamera == AppConfig.LOCAL_PHOTO_CAMERA_SELECT_ALG) {
            taskStepParams.setSelectAlgCamera(true);
        }else {
            taskStepParams.setSelectAlgCamera(false);
        }
        initJsonBuild = new StringBuilder();
        initJsonBuild.append("{");
        initJsonBuild.append("\"taskStepParams\":").append(mGson.toJson(taskStepParams)).append("}");




        renderJs(AppConfig.TASK_STEPS_VIEW_FILE,initJsonBuild.toString(),"分步",this);
        locationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//是否打开GPS
        option.setCoorType("bd0911");//设置返回值的坐标类型。
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        locationClient.setLocOption(option);
        mBDLocationListener = new MyBDLocationListener();
        // 注册监听
        locationClient.registerLocationListener(mBDLocationListener);
        locationClient.start();
//        locationClient.requestLocation();

        fragmentManager = getSupportFragmentManager();
        if(Build.VERSION.SDK_INT >= 28) {
            screenListener = new ScreenListener(this);
            screenListener.begin(screenStateListener);
        }
        homeKeyReceiver = new HomeKeyReceiver();
        IntentFilter filterTemp = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeKeyReceiver, filterTemp);
        PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
    }

    @Override
    public void onViewCreated(WXSDKInstance mWXSDKInstance, View view) {
        this.mWXSDKInstance = mWXSDKInstance;
        taskStepsView.addView(view);
    }

    @OnClick({R2.id.action_return,R2.id.indicator_upload_status})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            if(algRecordFragment != null && algRecordFragment.isAdded()) {
                if(!isRecorderFloating) {//录音页可见
                    if (isRecorder) {//录制中
                        showHintDialog(new DialogClickLinear() {
                                           @Override
                                           public void cancelClick() {

                                           }

                                           @Override
                                           public void defineClick() {
                                               onRecordFinish(false,stepId);
                                           }
                                       },this.getString(R.string.text_slicejobs_hint), "当前正在录音，你确定退出吗？",
                                this.getString(R.string.cancel), this.getString(R.string.confirm), false);
                    } else {
                        if (isRecorderEnd) {//录制完成
                            onRecordFinish(true,stepId);
                        } else {
                            onRecordFinish(false,stepId);
                        }
                    }
                }else{//录音页悬浮
                    ToastUtils.show("请先结束当前录音");
                }
            }else {
                if(uploadingProgress < 100 || (waitStepIndexs != null && waitStepIndexs.size() != 0)){//有正在缓存的任务
                    String notUploadIndexText = stepIndex + "";
                    if(waitStepIndexs != null && waitStepIndexs.size() != 0) {
                        notUploadIndexText = notUploadIndexText + "、";
                        for (int i = 0; i < waitStepIndexs.size(); i++) {
                            if (i != waitStepIndexs.size() - 1) {
                                notUploadIndexText = notUploadIndexText + waitStepIndexs.get(i) + "、";
                            } else {
                                notUploadIndexText = notUploadIndexText + waitStepIndexs.get(i);
                            }
                        }
                    }
                    showHintDialog(new DialogClickLinear() {
                                       @Override
                                       public void cancelClick() {

                                       }

                                       @Override
                                       public void defineClick() {
                                           if (mWXSDKInstance != null) {
                                               Map<String, Object> finishParams = new HashMap<>();
                                               finishParams.put("eventType", "back");
                                               mWXSDKInstance.fireGlobalEventCallback("taskStepsEvent", finishParams);
                                           }
                                           if(tempUploadIntent != null){
                                               tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
                                               startService(tempUploadIntent);
                                               stopService(tempUploadIntent);
                                               tempUploadIntent = null;
                                           }
                                           if(multiLineReceiver != null) {
                                               unregisterReceiver(multiLineReceiver);
                                               multiLineReceiver = null;
                                           }
                                           finish();
                                       }
                                   }, this.getString(R.string.text_slicejobs_hint), "当前任务有题目答案尚未上传，确定退出吗？",
                            "取消", "确定", false);
                }else {
                    if (mWXSDKInstance != null) {
                        Map<String, Object> finishParams = new HashMap<>();
                        finishParams.put("eventType", "back");
                        mWXSDKInstance.fireGlobalEventCallback("taskStepsEvent", finishParams);
                    }
                    if(tempUploadIntent != null){
                        tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
                        startService(tempUploadIntent);
                        stopService(tempUploadIntent);
                        tempUploadIntent = null;
                    }
                    if(multiLineReceiver != null) {
                        unregisterReceiver(multiLineReceiver);
                        multiLineReceiver = null;
                    }
                    finish();
                }
            }
        }else if(view.getId() == R.id.indicator_upload_status){
            showMultiUploadStatusView(false);
        }
    }


    @Override
    public void onTakeCamera(int takePhotoAuxiliaryLine) {
        evidenceRequest.setLocation(longitude + "," + latitude);
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            if(StringUtil.isNotBlank(evidenceRequest.getQuality()) && evidenceRequest.getQuality().equals(EvidenceRequest.PHOTO_QUALITY_HIGHEST)){
                doTakePhoto(cacheDir);//系统相机
            }else {
                int currCamera = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
                if (currCamera == AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG
                        || currCamera == AppConfig.LOCAL_PHOTO_CAMERA_SELECT_ALG) {
                    doMyTaskPhoto(cacheDir, evidenceRequest, takePhotoAuxiliaryLine);//爱零工相机
                } else {
                    doTakePhoto(cacheDir);//系统相机
                }
            }
        } else {
            toast(getResources().getString(R.string.text_no_sdcard));
        }
    }

    @Override
    public void onUploadOrUse(boolean forceCamera, boolean allowReusePhoto, String resultData) {
        evidenceRequest.setLocation(longitude + "," + latitude);
        doGetPhotoUploadOrUse(forceCamera,allowReusePhoto,resultData,cacheDir, evidenceRequest);
    }

    @Override
    public void onTakePhoto(boolean forceCamera, boolean allowReusePhoto, String resultData, int takePhotoAuxiliaryLine) {
        evidenceRequest.setLocation(longitude + "," + latitude);
        doGetPhotoAction(forceCamera,allowReusePhoto,resultData,cacheDir, evidenceRequest,takePhotoAuxiliaryLine);

    }


    @Override
    public void onTakeVideo(boolean forceCamera) {
        if (forceCamera) {
            cacheTaskVideo(cacheDir, evidenceRequest);
        } else {
            doGetVideoAction(cacheDir, evidenceRequest);
        }
    }

    @Override
    public void onTakeRecord(boolean forceCamera, String stepId) {
        if (forceCamera) {
            doTaskRecord(stepId,cacheDir, evidenceRequest,allowCrossSubjectRecord);
        } else {
            doGetRecordSelect(stepId,cacheDir, evidenceRequest,allowCrossSubjectRecord);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&(requestCode == CAMERA_WITH_DATA || requestCode == PHOTO_PICKED_WITH_DATA)) {
            Uri resultUri = requestCode == CAMERA_WITH_DATA ? mUriTemp
                    : Uri.parse("file://" + getPath(this, data.getData()));

            processPhoto(resultUri, new OnImageProcessedListener() {

                @Override
                public void onImageProcessed(Photo photo) {
                    if (photo.getIsClarity() == 0) {
                        if(photo.getImageRecognitionTypeMatch() == 0){//不开启图像识别或符合要求
                            if (mWXSDKInstance != null) {
                                Map<String, Object> params = new HashMap<>();
                                params.put("addPhotoPath", photo.getNativePhotoPath());
                                mWXSDKInstance.refreshInstance(params);
                            }
                        }
                    }
                }
            }, evidenceRequest);

        } else if (resultCode == RESULT_OK && requestCode == CAMERA_WITH_VIDEO) {//视屏返回
            String videoPath;
            String thumbnail;
            if (data != null) {
                if(data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_LIST_KEY) != null
                        && data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_LIST_KEY).size() != 0
                        && data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_PHOTO_LIST_KEY) != null
                        && data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_PHOTO_LIST_KEY).size() != 0) {//录制过程中有来电情况,多段视频
                    ArrayList<String> videoList = data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_LIST_KEY);
                    ArrayList<String> thumbnailList = data.getStringArrayListExtra(CameraKitActivity.TEMP_VIDEO_PHOTO_LIST_KEY);
                    ArrayList<ResultVideo> resultVideos = new ArrayList<>();
                    for (int i=0;i<videoList.size();i++){
                        String tampVideoPath = videoList.get(i);
                        String tampThumbnailPath = thumbnailList.get(i);
                        ResultVideo resultVideo = new ResultVideo();
                        resultVideo.setVideoUrl(tampVideoPath);
                        resultVideo.setThumbUrl(tampThumbnailPath);
                        resultVideos.add(resultVideo);
                    }
                    if (mWXSDKInstance != null) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("addVideoPaths", resultVideos);
                        mWXSDKInstance.refreshInstance(params);
                    }
                }else {
                    videoPath = data.getStringExtra(CameraKitActivity.TEMP_VIDEO_KEY);//视屏文件地址
                    thumbnail = data.getStringExtra(CameraKitActivity.TEMP_VIDEO_PHOTO_KEY);//照片文件地址

                    if (StringUtil.isNotBlank(videoPath) && StringUtil.isNotBlank(thumbnail)) {
                        MediaPlayer player = new MediaPlayer();
                        try {
                            player.setDataSource(videoPath);
                            player.prepare();
                            int duration = player.getDuration();//获取音频视频时长，单位毫秒
                            player.release();
                            if (duration >= evidenceRequest.getVideoDuration() * 1000) {
                                if (mWXSDKInstance != null) {
                                    Map<String, Object> params = new HashMap<>();
                                    ResultVideo resultVideo = new ResultVideo();
                                    resultVideo.setVideoUrl(videoPath);
                                    resultVideo.setThumbUrl(thumbnail);
                                    params.put("addVideoPath", resultVideo);
                                    mWXSDKInstance.refreshInstance(params);
                                }
                            } else {//视频时长不符合要求
                                showHintDialog(new DialogDefineClick() {
                                    @Override
                                    public void defineClick() {
                                        //FileOperateUtil.deleteVideoFile(videoPath, TaskStepsWebActivity.this);
                                    }
                                }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "此视频不符合要求拍摄时长，请重试", "我知道了", false);
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == VIDEO_PICKED_WITH_DATA) {//本地视屏返回
            String videoPath;
            String thumbnail;
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        // 视频ID:MediaStore.Audio.Media._ID
                        int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        // 视频名称：MediaStore.Audio.Media.TITLE
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                        // 视频路径：MediaStore.Audio.Media.DATA
                        videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                        // 视频时长：MediaStore.Audio.Media.DURATION
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        // 视频大小：MediaStore.Audio.Media.SIZE
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                        try {
                            thumbnail = FileUtil.getVideoThumbnail(this,videoPath,cacheDir);
                            if (StringUtil.isNotBlank(videoPath) && StringUtil.isNotBlank(thumbnail)) {
                                if(duration >= evidenceRequest.getVideoDuration()*1000){
                                    if (mWXSDKInstance != null) {
                                        Map<String, Object> params = new HashMap<>();
                                        ResultVideo resultVideo = new ResultVideo();
                                        resultVideo.setVideoUrl(videoPath);
                                        resultVideo.setThumbUrl(thumbnail);
                                        params.put("addVideoPath", resultVideo);
                                        mWXSDKInstance.refreshInstance(params);
                                    }
                                }else {//视频时长不符合要求
                                    showHintDialog(new DialogDefineClick() {
                                        @Override
                                        public void defineClick() {
                                            //FileOperateUtil.deleteVideoFile(videoPath, TaskStepsWebActivity.this);
                                        }
                                    }, this.getString(R.string.text_slicejobs_hint), "此视频不符合要求拍摄时长，请重试", "我知道了", false);
                                }
                            }
                        } catch (IOException e) {
                            umengCustomErrorLog("任务页面报错：userid:"+ BizLogic.getCurrentUser().userid+"任务id:" + task.getTaskid() + "获取视频缩略图报错");
                        }

                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == RECORD_PICKED_WITH_DATA) {//本地录音返回
            String recordPath;
            if (data != null) {
                Uri uri = data.getData();
                recordPath = FileUtil.getPath(this,uri);
                if (StringUtil.isNotBlank(recordPath)) {
                    MediaPlayer player = new MediaPlayer();
                    try {
                        player.setDataSource(recordPath);
                        player.prepare();
                        int duration =player.getDuration();//获取音频视频时长，单位毫秒
                        player.release();
                        if(duration >= evidenceRequest.getRecordDuration()*1000){
                            if (mWXSDKInstance != null) {
                                Map<String, Object> params = new HashMap<>();
                                params.put("addRecordPath", recordPath);
                                mWXSDKInstance.refreshInstance(params);
                            }
                        }else {//录音时长不符合要求
                            showHintDialog(new DialogDefineClick() {
                                @Override
                                public void defineClick() {
                                    //FileOperateUtil.deleteVideoFile(videoPath, TaskStepsWebActivity.this);
                                }
                            }, this.getString(R.string.text_slicejobs_hint), "此音频不符合要求录制时长，请重试", "我知道了", false);
                        }
                    } catch (IOException e) {
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == CODE_UPLOAD_REQUEST) {//上传界面返回（任务完成）
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == MYCAMERT_WITH_DATA) {//爱零工相机连拍模式返回
            if (data.getStringArrayListExtra("resultPhotos") != null) {
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("addPhotoPaths", data.getStringArrayListExtra("resultPhotos"));
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == OPENCV_CAMERT_WITH_DATA) {//openCv全景相机返回

            if (data.getStringArrayListExtra("resultPhotos") != null) {
                panoPhotos.addAll(data.getStringArrayListExtra("resultPhotos"));
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("addPhotoPaths", data.getStringArrayListExtra("resultPhotos"));
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == CAMERA_WITH_DATA) {
            PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putBoolean(AppConfig.USER_STYTEM_CAMERA__KEY, false);
        }else if(resultCode == RESULT_OK && requestCode == BROWER_IMAGE_REQUEST_CODE){//浏览图片返回
            boolean ifBrowserFinished = data.getBooleanExtra("ifBrowserFinished", false);
            if(ifBrowserFinished){
                if (mWXSDKInstance != null) {
                    Map<String, Object> finishParams = new HashMap<>();
                    finishParams.put("changedIndex", "-1");
                    mWXSDKInstance.fireGlobalEventCallback("exampleComponentEvent", finishParams);
                }
            }
        }else if(resultCode == RESULT_OK && requestCode == BROWER_CACHE_IMAGE_REQUEST_CODE){//浏览证据图片返回
            ArrayList<String> urls = data.getStringArrayListExtra("urls");
            if(urls != null){
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("updatePhotoPaths", urls);
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        }else if(resultCode == RESULT_OK && requestCode == TASK_PHOTO_PICKED_WITH_DATA){//选择已经拍照证据图片返回
            ArrayList<String> urls = data.getStringArrayListExtra("selectPhotos");
            if(urls != null){
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("addPhotoPaths", urls);
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        }else if(resultCode == RESULT_OK && requestCode == MULTI_PHOTO_PICKED_WITH_DATA){//多选图库图片返回
            List<Uri> mSelectedPhotos = Matisse.obtainResult(data);
            if (mSelectedPhotos != null) {
                List<String> photoPathList = new ArrayList<>();
                for (int i = 0; i < mSelectedPhotos.size(); i++) {
                    Uri uri = mSelectedPhotos.get(i);
                    String photoPath = FileUtil.getPath(this, uri);
                    photoPathList.add(photoPath);
                }
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("addPhotoPaths", photoPathList);
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        }
    }




    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
        }
        unregisterReceiver(homeKeyReceiver);//取消home键监听注册
        if(screenListener != null) {
            screenListener.unregisterListener();
        }
        if(tempUploadIntent != null){
            tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
            startService(tempUploadIntent);
            stopService(tempUploadIntent);
            tempUploadIntent = null;
        }
        if(multiLineReceiver != null) {
            unregisterReceiver(multiLineReceiver);
            multiLineReceiver = null;
        }
    }



    /**
     * 由h5触发接口
     * @param event
     */
    @Subscribe
    public void onTaskStepViewEvent(AppEvent.TaskStepViewEvent event) {
        if (StringUtil.isBlank(event.eventType)) {
            return;
        } else if (event.eventType.equals("getEvidence")) {
            String evidenceType = TaskStep.EVIDENCETYPE_PHPTO;
            if (null != event.params.get("evidenceType")) {
                evidenceType = event.params.get("evidenceType").toString();
            }

            setEvidenceRequest(event.params);
            if (evidenceType.equals(TaskStep.EVIDENCETYPE_PHPTO)) {//照片
                String photoSource = null;
                if(event.params.get("photoSource") != null && !event.params.get("photoSource").toString().equals("all")) {
                    photoSource = (String) event.params.get("photoSource");
                }
                if (event.params.get("forceCamera") != null) {
                    forceCamera = (boolean) event.params.get("forceCamera");
                }
                if (event.params.get("resultData") != null) {
                    resultData = event.params.get("resultData").toString();
                }
                if (event.params.get("allowReusePhoto") != null) {
                    allowReusePhoto = (boolean) event.params.get("allowReusePhoto");
                }
                if (event.params.get("takePhotoAuxiliaryLine") != null) {
                    takePhotoAuxiliaryLine = (int) event.params.get("takePhotoAuxiliaryLine");
                }
                onTakePhoto(forceCamera, allowReusePhoto, resultData,takePhotoAuxiliaryLine);
            } else if (evidenceType.equals(TaskStep.EVIDENCETYPE_RECORD)) {//音频
                boolean ifForceCamera = (boolean) event.params.get("forceCamera");
                if (algRecordFragment == null) {//如果没有开始的录音才取录音相关参数，防止有跨题录音时，参数被后一道录音题覆盖,保存错误
                    stepId = (String) event.params.get("stepId");
                    if(event.params.get("allowCrossSubjectRecord") != null) {
                        allowCrossSubjectRecord = (boolean) event.params.get("allowCrossSubjectRecord");
                    }
                    onTakeRecord(ifForceCamera,stepId);
                } else {
                    ToastUtils.show("请先结束当前录音");
                }
            } else if (evidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO)) {//视频
                boolean ifForceCamera = (boolean) event.params.get("forceCamera");
                onTakeVideo(ifForceCamera);
            }
        } else if (event.eventType.equals("uploadEvidence")) {//立即上传
            MiniTask miniTask = new MiniTask();
            miniTask.setTaskid(task.getTaskid());
            miniTask.setOrderid(task.getOrderid());
            miniTask.setRfversion(task.getRfversion());
            miniTask.setLongitude(task.getLongitude());
            miniTask.setLatitude(task.getLatitude());
            if(StringUtil.isNotBlank(task.getReview()) && Integer.parseInt(task.getReview()) > 0){
                miniTask.setIfFirstCommit(false);
            }
            if (StringUtil.isNotBlank(task.getMarket_gatherinfo())) {
                miniTask.setMarket_gatherinfo(task.getMarket_gatherinfo());
            }
            if (event.params.get("miniTemplatejson") != null) {
                List<MiniTaskStep> miniTaskSteps = mGson.fromJson(
                        event.params.get("miniTemplatejson").toString(), new TypeToken<List<MiniTaskStep>>() {
                        }.getType());
                if (miniTaskSteps != null) {
                    miniTask.setTaskSteps(miniTaskSteps);
                }
            }

            Intent intent = UploadCacheActivity.getStartIntent(
                    TaskStepsWebActivity.this,
                    miniTask,
                    event.params.get("resultData").toString(),
                    event.params.get("checkinlocation").toString(),
                    event.params.get("sec_consumed").toString(),
                    event.params.get("interrupted_times").toString(),
                    event.params.get("outrange_times").toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
            startActivityForResult(intent, CODE_UPLOAD_REQUEST);
        } else if (event.eventType.equals("cacheEvidence")) {//缓存上传
            openPackage(event.params.get("resultData").toString(), event.params.get("checkinlocation").toString());
        } else if (event.eventType.equals("popTaskStepView")) {
            if(tempUploadIntent != null){
                tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
                startService(tempUploadIntent);
                stopService(tempUploadIntent);
                tempUploadIntent = null;
            }
            if(multiLineReceiver != null) {
                unregisterReceiver(multiLineReceiver);
                multiLineReceiver = null;
            }
            if(event.params != null && event.params.get("closeCause") != null) {
                Intent backTaskDetail = new Intent();
                if (event.params.get("closeCause").toString().equals("finishTask")) {
                    backTaskDetail.putExtra("updateType", "taskCommitSuccess");
                } else if (event.params.get("closeCause").toString().equals("closeTask")) {
                    backTaskDetail.putExtra("updateType", "all");
                }
                setResult(RESULT_OK,backTaskDetail);
            }
            finish();
        } else if (event.eventType.equals("showExampleDialog")) {//显示对话框
            TaskStep taskStep = new TaskStep();

             setEvidenceRequest(event.params);
            if (null != event.params.get("resultData")) {
                resultData = event.params.get("resultData").toString();
            }
             if (null != event.params.get("forceCamera")) {
                 forceCamera = (boolean) event.params.get("forceCamera");
                 taskStep.setForceCamera(forceCamera);
             }
             if (null != event.params.get("allowReusePhoto")) {
                allowReusePhoto = (boolean) event.params.get("allowReusePhoto");
                taskStep.setAllowReusePhoto(allowReusePhoto);
             }

             List<String> examplePhotos = new ArrayList<>(); // 拍照实例
            if (null != event.params.get("examplePhotos")) {
                examplePhotos.addAll((Collection<? extends String>) event.params.get("examplePhotos"));
                taskStep.setExamplePhotos(examplePhotos);
            }

            String exampleText;
            if (null != event.params.get("exampleText")) {
                exampleText = event.params.get("exampleText").toString();
                taskStep.setExampleText(exampleText);
            }

            String evidenceType = TaskStep.EVIDENCETYPE_PHPTO;//当选题的结果类型
            if (null != event.params.get("evidenceType")) {
                evidenceType = event.params.get("evidenceType").toString();
                taskStep.setEvidenceType(evidenceType);
            }

            if (null != event.params.get("stepId")) {
                stepId = event.params.get("stepId").toString();
                taskStep.setStepId(stepId);
            }

            if (null != event.params.get("takePhotoAuxiliaryLine")) {
                takePhotoAuxiliaryLine = (int) event.params.get("takePhotoAuxiliaryLine");
                taskStep.setTakePhotoAuxiliaryLine(takePhotoAuxiliaryLine);
            }

            PhotoRequirement requirement = new PhotoRequirement(taskStep);;
            UploadPhotoDialogFragment dialog = UploadPhotoDialogFragment.newInstance(requirement,resultData);
            dialog.show(TaskStepsWebActivity.this.getSupportFragmentManager(), "upload");
            /*Window dialogWindow = dialog.getDialog().getWindow();
            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
            p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.7，根据实际情况调整
            dialogWindow.setAttributes(p);*/
        } else if (event.eventType.equals("lookEvidence")) {//查看凭证
            String url = null;
                if (null != event.params.get("url")) {
                    url = event.params.get("url").toString();
                }

            String evidenceType = TaskStep.EVIDENCETYPE_PHPTO;
            if (null != event.params.get("evidenceType")) {
                evidenceType = event.params.get("evidenceType").toString();
            }

            if (StringUtil.isBlank(url)) {
                return;
            }

            String urlWithScheme = url.startsWith("/") ? "file://" + url : url;

            if (evidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                if (urlWithScheme.contains("http://")) {
                    if(!NetWorkUtil.isWifiActive()) {//检查是否wifi，直接播放
                        showHintDialog(new DialogClickLinear() {
                                           @Override
                                           public void cancelClick() {

                                           }

                                           @Override
                                           public void defineClick() {
                                               startActivity(MediaPlayerActivity.getIntent(TaskStepsWebActivity.this,urlWithScheme));
                                           }
                                       }, this.getString(R.string.text_slicejobs_hint), "非wifi环境播放会消耗一些流量，确定要立即播放吗？",
                                this.getString(R.string.cancel), this.getString(R.string.confirm), false);
                    }else {
                        startActivity(MediaPlayerActivity.getIntent(this,urlWithScheme));
                    }
                }else {
                    startActivity(MediaPlayerActivity.getIntent(this,urlWithScheme));
                }
            } else if (evidenceType.equals(TaskStep.EVIDENCETYPE_RECORD)) {
                if (urlWithScheme.contains("http://")) {
                    if (NetWorkUtil.isWifiActive()) {//检查是否wifi，直接播放
                        startActivity(MediaPlayerActivity.getIntent(this,urlWithScheme));
                    } else {
                        showHintDialog(new DialogClickLinear() {
                                           @Override
                                           public void cancelClick() {

                                           }

                                           @Override
                                           public void defineClick() {
                                               startActivity(MediaPlayerActivity.getIntent(TaskStepsWebActivity.this,urlWithScheme));
                                           }
                                       }, this.getString(R.string.text_slicejobs_hint), "非wifi环境播放会消耗一些流量，确定要立即播放吗？",
                                this.getString(R.string.cancel), this.getString(R.string.confirm), false);
                    }
                } else {
                    startActivity(MediaPlayerActivity.getIntent(this,urlWithScheme));
                }
            } else {
                ArrayList<String> urlList = new ArrayList<>();
                ArrayList<String> urlListNew = new ArrayList<>();
                if(event.params != null){
                    urlList.addAll((Collection<? extends String>) event.params.get("urls"));
                    if(urlList != null && urlList.size() != 0){
                        int position = urlList.indexOf(url);
                        for(int i=0;i<urlList.size();i++){
                            String mUrl = urlList.get(i);
                            urlListNew.add(mUrl);
                        }
                        if(event.params.get("canDelete") != null) {
                            TaskStepsWebActivity.this.startActivityForResult(ViewImageActivity.getIntent(TaskStepsWebActivity.this, urlListNew, position, true, (Boolean) event.params.get("canDelete")), BROWER_CACHE_IMAGE_REQUEST_CODE);
                        }else{
                            TaskStepsWebActivity.this.startActivityForResult(ViewImageActivity.getIntent(TaskStepsWebActivity.this, urlListNew, position, true, true), BROWER_CACHE_IMAGE_REQUEST_CODE);
                        }
                    }
                }
            }
        } else if (event.eventType.equals("openImageBrowser")) {
            if(event.params != null){
                if(event.params.get("url") != null){
                    String url = (String) event.params.get("url");
                    if(url != null && !url.isEmpty()){
                        TaskStepsWebActivity.this.startActivity(ViewImageActivity.getIntent(TaskStepsWebActivity.this, url));
                    }
                }else if(event.params.get("urls") != null){
                    JSONArray urlArray = (JSONArray) event.params.get("urls");
                    if(urlArray != null && urlArray.size() != 0){
                        int position = (int) event.params.get("index");
                        ArrayList<String> urlList = new ArrayList<>();
                        for (int i=0;i<urlArray.size();i++){
                            urlList.add(urlArray.get(i).toString());
                        }
                        TaskStepsWebActivity.this.startActivityForResult(ViewImageActivity.getIntent(TaskStepsWebActivity.this, urlList, position), BROWER_IMAGE_REQUEST_CODE);
                    }
                }

            }
        } else if (event.eventType.equals("setStepLocation")) {
            if (event.params != null && event.params.size() != 0) {
                String location = event.params.get("location").toString();
                if (StringUtil.isNotBlank(location) && !location.equals("0,0")) {
                    evidenceRequest.setLocation(location);
                } else {
                    evidenceRequest.setLocation(PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.USER_LOCATION_KEY, "0,0"));
                }
            }
        }  else if (event.eventType.equals("showProgress")) {//打开普通进度条
            progressDialog = new LoadingDialog(this);
            TaskStepsWebActivity.this.showProgressDialog();
        } else if (event.eventType.equals("hideProgress")) {//关闭普通进度条
            TaskStepsWebActivity.this.dismissProgressDialog();
        }else if(event.eventType.equals("showToast")){
            String msg = (String) event.params.get("message");
            if(msg != null && StringUtil.isNotBlank(msg)){
                toast(msg);
            }
        } else if (event.eventType.equals("goGatherMarketInfo")) {//去门店收集任务
            MiniTask miniTask = new MiniTask();
            miniTask.setTaskid(task.getTaskid());
            miniTask.setOrderid(task.getOrderid());
            miniTask.setRfversion(task.getRfversion());
            miniTask.setLongitude(task.getLongitude());
            miniTask.setLatitude(task.getLatitude());
            if(StringUtil.isNotBlank(task.getReview()) && Integer.parseInt(task.getReview()) > 0){
                miniTask.setIfFirstCommit(false);
            }
            if (StringUtil.isNotBlank(task.getMarket_gatherinfo())) {
                miniTask.setMarket_gatherinfo(task.getMarket_gatherinfo());
            }
            if (event.params.get("miniTemplatejson") != null) {
                List<MiniTaskStep> miniTaskSteps = mGson.fromJson(
                        event.params.get("miniTemplatejson").toString(), new TypeToken<List<MiniTaskStep>>() {
                        }.getType());
                if (miniTaskSteps != null) {
                    miniTask.setTaskSteps(miniTaskSteps);
                }
            }
            Intent intent = MarketInfoGatherTaskStepsActivity.getStartIntent(
                    TaskStepsWebActivity.this,
                    miniTask,
                    event.params.get("resultData").toString(),
                    event.params.get("checkinlocation").toString(),
                    event.params.get("sec_consumed").toString(),
                    event.params.get("interrupted_times").toString(),
                    event.params.get("outrange_times").toString(),
                    (Boolean) event.params.get("isHaveEvidence"));
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
            startActivityForResult(intent, CODE_UPLOAD_REQUEST);
        } else if (event.eventType.equals("ifSelectAlgCamera")) {//是否使用爱零工相机
            boolean ifSelectAlgCamera = (boolean) event.params.get("ifSelectAlgCamera");
            if(ifSelectAlgCamera){
                PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
            }else {
                PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_SYSTEM);
            }
        }else if (event.eventType.equals("tempUploadJson")) {
            String tempUploadJson = (String) event.params.get("taskStepResultJson");
            if (StringUtil.isNotBlank(tempUploadJson)) {
                String tempJsonKey = System.currentTimeMillis() + "";
                UPLOADJSONMAP.clear();
                UPLOADJSONMAP.put(tempJsonKey, tempUploadJson);
                if(tempUploadIntent == null){
                    tempUploadIntent = new Intent();
                    tempUploadIntent.setAction("com.slicejobs.algsdk.algtasklibrary.TASK_RESULT_UPLOAD_SERVICE");
                    tempUploadIntent.setPackage(getPackageName());
                }
                tempUploadMiniTask = new MiniTask();
                tempUploadMiniTask.setTaskid(task.getTaskid());
                tempUploadMiniTask.setOrderid(task.getOrderid());
                tempUploadMiniTask.setRfversion(task.getRfversion());
                if(StringUtil.isNotBlank(task.getReview()) && Integer.parseInt(task.getReview()) > 0){
                    tempUploadMiniTask.setIfFirstCommit(false);
                }
                if (event.params.get("miniTemplatejson") != null) {
                    List<MiniTaskStep> miniTaskSteps = mGson.fromJson(
                            event.params.get("miniTemplatejson").toString(), new TypeToken<List<MiniTaskStep>>() {
                            }.getType());
                    if (miniTaskSteps != null) {
                        tempUploadMiniTask.setTaskSteps(miniTaskSteps);
                    }
                }
                if (event.params.get("uploadStatus") != null) {
                    tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, event.params.get("uploadStatus").toString());
                }

                if (event.params.get("isCommit") != null) {
                    isCommit = (boolean) event.params.get("isCommit");
                }else {
                    isCommit = false;
                }

                if (event.params.get("stepNumber") != null) {
                    stepIndex = event.params.get("stepNumber").toString();
                }

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(TaskResultUploadService.ACTION_MULTI_UPLOAD_LINES_STATUS);
                intentFilter.addAction(TaskResultUploadService.ACTION_MULTI_UPLOAD_ERROR);
                if(multiLineReceiver == null) {
                    multiLineReceiver = new MultiUploadReceiver();
                    registerReceiver(multiLineReceiver, intentFilter);
                }

                addMultiUploadStatusView();

                tempUploadIntent.putExtra(TEMP_UPLOAD_MINITASK_KEY, tempUploadMiniTask);
                tempUploadIntent.putExtra(TEMP_UPLOAD_CACHEDIR_KEY, cacheDir);
                tempUploadIntent.putExtra(TEMP_UPLOAD_HASHMAP_JSON_KEY, tempJsonKey);
                tempUploadIntent.putExtra(TEMP_UPLOAD_STEP_INDEX_KEY, stepIndex);
                tempUploadIntent.putExtra(TEMP_UPLOAD_ISCOMMIT_KEY, isCommit);
                startService(tempUploadIntent);

                uploadStatusIndicator.setVisibility(View.VISIBLE);
                uploadStatusIndicator.setBackgroundResource(R.drawable.shape_orange_circle);
                if(isCommit){
                    showMultiUploadStatusView(false);
                }
            }
        }
    }


    private void setEvidenceRequest(Map<String, Object> params) {
        String evidenceQuality = "2";
        if (null != params.get("evidenceQuality")) {
            evidenceQuality = params.get("evidenceQuality").toString();
        }

        float clarity = 0;
        try {
            if (null != params.get("clarity") && StringUtil.isNotBlank(params.get("clarity").toString())) {
                clarity = Float.parseFloat(params.get("clarity").toString());//照片清晰度
            }
        } catch (NumberFormatException e) {

        }

        int videoDuration = 0;

        if (null != params.get("videoDuration")) {
            videoDuration = (int)params.get("videoDuration");
        }


        int recordDuration = 0;
        if (null != params.get("recordDuration")) {
            recordDuration = (int) params.get("recordDuration");
        }

        evidenceRequest.setQuality(evidenceQuality);
        evidenceRequest.setClarity(clarity);
        evidenceRequest.setVideoDuration(videoDuration);
        evidenceRequest.setRecordDuration(recordDuration);
    }







    /**
     * 缓存上传,打离线包
     */
    private void openPackage(String resultJson, String location) {
        File file = new File(cacheDir);
        if (file.exists()) {
            File packFile = new File(cacheDir + File.separator + CACHE_FILE_NAME);
            if (packFile.exists()) {
                packFile.delete();
            }
            try {
                packFile.createNewFile();
                //将定位打包
                String result = location+CACHE_SPACE+resultJson;
                if (FileUtil.writeFileSdcardFile(packFile.getAbsolutePath(), result)) {
                    //toast("缓存保存成功，请在任务到期前上传");
                    toast("缓存保存成功，请在任务到期前上传");
                    setResult(CACHE_RESULT_GOBACK);
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //toast("保存失败");
                toast("保存失败");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        if(ifHomePressed){
            if(algRecordFragment != null && algRecordFragment.isAdded()){
                if(isRecorder){
                    startFloatingService(FloatingRecordService.FLOAT_REORD_STATUS_BACK_TO_RESUME,algRecordFragment.currentRecordTime);
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private class MyBDLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && bdLocation.getLongitude() != 4.9E-324 && bdLocation.getLatitude() != 4.9E-324 && bdLocation.getLongitude() != 0 && bdLocation.getLatitude() != 0) {//获取到定位
                // 根据BDLocation 对象获得经纬度以及详细地址信息
                latitude = bdLocation.getLatitude();
                longitude = bdLocation.getLongitude();
              //  PrefUtil.make(TaskStepsWebActivity.this, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.USER_LOCATION_INFO,bdLocation);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(algRecordFragment != null && algRecordFragment.isAdded()) {
            if(!isRecorderFloating) {//录音页可见
                if (isRecorder) {//录制中
                    showHintDialog(new DialogClickLinear() {
                                       @Override
                                       public void cancelClick() {

                                       }

                                       @Override
                                       public void defineClick() {
                                           onRecordFinish(false,stepId);
                                       }
                                   }, this.getString(R.string.text_slicejobs_hint), "当前正在录音，你确定退出吗？",
                            this.getString(R.string.cancel), this.getString(R.string.confirm), false);
                } else {
                    if (isRecorderEnd) {//录制完成
                        onRecordFinish(true,stepId);
                    } else {
                        onRecordFinish(false,stepId);
                    }
                }
            }else{//录音页悬浮
                ToastUtils.show("请先结束当前录音");
            }
        }else {
            if(uploadingProgress < 100 || (waitStepIndexs != null && waitStepIndexs.size() != 0)){//有正在缓存的任务
                showHintDialog(new DialogClickLinear() {
                                   @Override
                                   public void cancelClick() {

                                   }

                                   @Override
                                   public void defineClick() {
                                       if (mWXSDKInstance != null) {
                                           Map<String, Object> finishParams = new HashMap<>();
                                           finishParams.put("eventType", "back");
                                           mWXSDKInstance.fireGlobalEventCallback("taskStepsEvent", finishParams);
                                       }
                                       if(tempUploadIntent != null){
                                           tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
                                           startService(tempUploadIntent);
                                           stopService(tempUploadIntent);
                                           tempUploadIntent = null;
                                       }
                                       if(multiLineReceiver != null) {
                                           unregisterReceiver(multiLineReceiver);
                                           multiLineReceiver = null;
                                       }
                                       finish();
                                   }
                               }, this.getString(R.string.text_slicejobs_hint), "当前任务有题目答案尚未上传，确定退出吗？",
                        "取消", "确定", false);
            }else {
                if (mWXSDKInstance != null) {
                    Map<String, Object> finishParams = new HashMap<>();
                    finishParams.put("eventType", "back");
                    mWXSDKInstance.fireGlobalEventCallback("taskStepsEvent", finishParams);
                }
                if(tempUploadIntent != null){
                    tempUploadIntent.putExtra(TEMP_UPLOAD_STATUS_KEY, "exit");
                    startService(tempUploadIntent);
                    stopService(tempUploadIntent);
                    tempUploadIntent = null;
                }
                if(multiLineReceiver != null) {
                    unregisterReceiver(multiLineReceiver);
                    multiLineReceiver = null;
                }
                finish();
            }
        }
    }

    /**
     * 录制录音
     * @param dir
     */
    protected void doTaskRecord(String stepId, String dir, EvidenceRequest evidenceRequest, boolean allowCrossSubjectRecord) {
        TaskStepsWebActivityPermissionsDispatcher.openFloatSlicejobsRecordWithCheck(this, stepId,dir, evidenceRequest,allowCrossSubjectRecord);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void openFloatSlicejobsRecord(String stepId, String dir, EvidenceRequest evidenceRequest, boolean allowCrossSubjectRecord) {
        if(algRecordFragment == null) {
            algRecordFragment = new ALGRecordFragment();
            FileUtil.createDirIfNotExisted(cacheDir + "/record/", false);
            currRecordPath = cacheDir + "/record/" + System.currentTimeMillis() + ".mp3";
            File file = new File(currRecordPath);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bundle bundle = new Bundle();
            bundle.putString("source", ALGRecordFragment.SOURCE_RECORD);
            bundle.putString("filePath", currRecordPath);
            bundle.putString("stepId", stepId);
            bundle.putBoolean("allowCrossSubjectRecord", allowCrossSubjectRecord);
            bundle.putSerializable(ALGRecordFragment.EVIDENCE_REQUEST, evidenceRequest);
            algRecordFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.rootView, algRecordFragment);
            fragmentTransaction.commit();
            algRecordFragment.setAlgRecordFinishListener(this);
        }else {
            ToastUtils.show("请先结束当前录音");
        }

    }

    //用户点击不再询问这个权限时调用
    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    void onRecordAudioNeverAskAgain() {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "录音被禁用", "请在手机“设置-应用程序权限管理-选择本App”允许启动麦克风或录音", "以后再说", "打开", false);
    }


    private void doGetRecordSelect(String stepId, String cacheDir, EvidenceRequest evidenceRequest, boolean allowCrossSubjectRecord){
        new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("录音", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                String status = Environment.getExternalStorageState();
                                if (status.equals(Environment.MEDIA_MOUNTED)) {
                                    doTaskRecord(stepId,cacheDir, evidenceRequest,allowCrossSubjectRecord);
                                } else {
                                    //Toast.makeText(PickPhotoActivity.this, r.string.text_no_sdcard, Toast.LENGTH_SHORT).show();
                                    toast(getResources().getString(R.string.text_no_sdcard));
                                }
                            }
                        })
                .addSheetItem("从手机选择", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                doPickRecordFromGallery();
                            }
                        }).show();
    }

    @Override
    public void onRecording(boolean isRecorder, boolean isRecorderEnd) {
        this.isRecorder = isRecorder;
        this.isRecorderEnd = isRecorderEnd;
    }

    @Override
    public void onRecordFinish(boolean ifSaveRdcord, String stepId) {
        if(algRecordFragment != null && algRecordFragment.isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(algRecordFragment);
            fragmentTransaction.commit();
            algRecordFragment = null;
        }
        if(ifSaveRdcord){
            if (StringUtil.isNotBlank(currRecordPath)) {
                MediaPlayer player = new MediaPlayer();
                try {
                    player.setDataSource(currRecordPath);
                    player.prepare();
                    int duration =player.getDuration();//获取音频视频时长，单位毫秒
                    player.release();
                    if(duration >= evidenceRequest.getRecordDuration()*1000){
                        if (mWXSDKInstance != null) {
                            if(!allowCrossSubjectRecord) {
                                Map<String, Object> params = new HashMap<>();
                                params.put("addRecordPath", currRecordPath);
                                params.put("taskRecordStatus", "0");
                                mWXSDKInstance.refreshInstance(params);
                            }else {
                                Map<String, Object> params = new HashMap<>();
                                params.put("recordStepId", stepId);
                                params.put("recordPath", currRecordPath);
                                mWXSDKInstance.fireGlobalEventCallback("saveRecordEvent", params);
                            }
                        }
                    }else {//录音时长不符合要求
                        showHintDialog(new DialogDefineClick() {
                            @Override
                            public void defineClick() {
                                //FileOperateUtil.deleteVideoFile(videoPath, TaskStepsWebActivity.this);
                            }
                        }, this.getString(R.string.text_slicejobs_hint), "此音频不符合要求录制时长，请重试", "我知道了", false);
                    }
                } catch (IOException e) {
                }
            }
        } else {
            if (mWXSDKInstance != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("taskRecordStatus", "0");
                mWXSDKInstance.refreshInstance(params);
            }
        }
    }

    @Override
    public void onRecordFloatint(boolean ifFloating) {
        isRecorderFloating = ifFloating;
        if (!allowCrossSubjectRecord) {
            if (mWXSDKInstance != null) {
                Map<String, Object> params = new HashMap<>();
                if (ifFloating) {
                    params.put("taskRecordStatus", "1");//不允许跨题录音，录音悬浮时修改js taskRecordStatus为1
                } else {
                    params.put("taskRecordStatus", "0");//不允许跨题录音，录音不悬浮时修改js taskRecordStatus为0
                }
                mWXSDKInstance.refreshInstance(params);
            }
        } else {//允许跨题录音，录音悬浮时修改js taskRecordStatus为2
            if(isRecorder) {
                if (mWXSDKInstance != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("taskRecordStatus", "2");
                    mWXSDKInstance.refreshInstance(params);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRecordPause() {
        startFloatingService(FloatingRecordService.FLOAT_REORD_STATUS_PAUSE,algRecordFragment.currentRecordTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRecordResume() {
        startFloatingService(FloatingRecordService.FLOAT_REORD_STATUS_RESUME,algRecordFragment.currentRecordTime);
    }

    private ScreenListener.ScreenStateListener screenStateListener = new ScreenListener.ScreenStateListener() {
        @Override
        public void onScreenOn() {

        }

        @Override
        public void onScreenOff() {
            if(algRecordFragment != null && algRecordFragment.isAdded()){
                if(isRecorder){
                    algRecordFragment.pauseRecord();
                }
            }
        }

        @Override
        public void onUserPresent() {
            if(algRecordFragment != null && algRecordFragment.isAdded()){
                if(isRecorder){
                    algRecordFragment.resumeRecord();
                }
            }
        }

        @Override
        public void onHomePressed() {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {//音量-
            if(algRecordFragment != null) {
                if (!isRecorder) {//开始录音
                    algRecordFragment.clickStartRecord();
                } else if(isRecorder) {//停止录音
                    algRecordFragment.clickStopRecord();
                }
                return true;
            }
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {//音量+
            if(algRecordFragment != null) {
                if (!isRecorder) {//开始录音
                    algRecordFragment.clickStartRecord();
                } else if(isRecorder) {//停止录音
                    algRecordFragment.clickStopRecord();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            return true;
        }else{

        }
        return super.onKeyUp(keyCode, event);
    }

    private UploadTaskStepResultTask.UploadTaskStepResultListener uploadResultListener = new UploadTaskStepResultTask.UploadTaskStepResultListener() {
        @Override
        public void onUploadFinished(String result) {
            if(uploadTaskStepResultTask != null) {
                uploadTaskStepResultTask.cancel(true);
                uploadTaskStepResultTask = null;
                System.gc();
            }
            if (mWXSDKInstance != null && !ifNotifyedFlag) {
                Map<String, Object> tempUploadParams = new HashMap<>();
                tempUploadParams.put("eventType", "success");
                tempUploadParams.put("taskStepResultJson", result);
                mWXSDKInstance.fireGlobalEventCallback("tempUploadJsonEvent", tempUploadParams);
                ifNotifyedFlag = true;
            }
        }

        @Override
        public void onUploadError() {
            if(uploadTaskStepResultTask != null) {
                uploadTaskStepResultTask.cancel(true);
                uploadTaskStepResultTask = null;
                System.gc();
            }
            if (mWXSDKInstance != null && !ifNotifyedFlag) {
                Map<String, Object> tempUploadParams = new HashMap<>();
                tempUploadParams.put("eventType", "failed");
                mWXSDKInstance.fireGlobalEventCallback("tempUploadJsonEvent", tempUploadParams);
                ifNotifyedFlag = true;
            }
        }
    };

    private class MultiUploadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TaskResultUploadService.ACTION_MULTI_UPLOAD_LINES_STATUS)) {
                uploadingProgress = intent.getIntExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY, 0);
                waitStepIndexs = intent.getStringArrayListExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY);
                uploadedStepIndexs = intent.getStringArrayListExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY);
                boolean isUpdate = intent.getBooleanExtra(TaskResultUploadService.MULTI_UPLOAD_IS_UPDATE_KEY, false);
                taskUploadProgressFragment.setUploadFail(false);
                if(uploadingProgress == 100){
                    uploadStatusIndicator.setBackgroundResource(R.drawable.shape_green_circle);
                    taskUploadProgressFragment.refreshUploadingStatus(true);
                }else {
                    uploadStatusIndicator.setBackgroundResource(R.drawable.shape_orange_circle);
                    taskUploadProgressFragment.refreshUploadingStatus(false);
                }
                String uploadingIndex = intent.getStringExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_INDEX_KEY);
                if(StringUtil.isNotBlank(uploadingIndex)) {
                    taskUploadProgressFragment.setUpdateingIndex(uploadingIndex);
                }
                taskUploadProgressFragment.setIsUpdate(isUpdate);
                taskUploadProgressFragment.setCurrentProgress(uploadingProgress);
                if(waitStepIndexs != null){
                    taskUploadProgressFragment.setWaitUploadList(waitStepIndexs);
                }
                if(uploadedStepIndexs != null){
                    taskUploadProgressFragment.setUploadedList(uploadedStepIndexs);
                }
                String tempJsonKey = intent.getStringExtra(TaskResultUploadService.MULTI_UPLOAD_UPLOADED_JSON_MAP_KEY);
                boolean isUploadFinish = intent.getBooleanExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_LINE_FINISH_KEY, false);
                if(isCommit && isUploadFinish && waitStepIndexs.size() == 0){
                    if (mWXSDKInstance != null) {
                        if (StringUtil.isNotBlank(tempJsonKey)) {
                            if(!isShowToCacheViewDialog) {
                                showHintDialog(new DialogClickLinear() {
                                                   @Override
                                                   public void cancelClick() {
                                                       isShowToCacheViewDialog = false;
                                                   }

                                                   @Override
                                                   public void defineClick() {
                                                       String commitJson = UPLOADJSONMAP.get(tempJsonKey);
                                                       if(StringUtil.isNotBlank(commitJson)){
                                                           Map<String, Object> tempUploadParams = new HashMap<>();
                                                           tempUploadParams.put("eventType", "success");
                                                           tempUploadParams.put("taskStepResultJson", commitJson);
                                                           mWXSDKInstance.fireGlobalEventCallback("ossUploadJsonEvent", tempUploadParams);
                                                           isShowToCacheViewDialog = false;
                                                       }
                                                   }
                                               }, "", TaskStepsWebActivity.this.getString(R.string.text_cache_finlish_if_commit),
                                        TaskStepsWebActivity.this.getString(R.string.cancel), TaskStepsWebActivity.this.getString(R.string.confirm), false);
                                isShowToCacheViewDialog = true;
                            }
                        }
                    }
                }
            }else if(intent.getAction().equals(TaskResultUploadService.ACTION_MULTI_UPLOAD_ERROR)){
                if(intent.getSerializableExtra(TaskResultUploadService.MULTI_UPLOAD_ERROR_JSON_KEY) != null) {
                    uploadStatusIndicator.setBackgroundResource(R.drawable.shape_red_circle);
                    taskUploadProgressFragment.refreshUploadingStatus(false);
                    TempUploadTask failTempUploadTask = (TempUploadTask) intent.getSerializableExtra(TaskResultUploadService.MULTI_UPLOAD_ERROR_JSON_KEY);
                    taskUploadProgressFragment.setUploadFail(true);
                    taskUploadProgressFragment.setFailTempUploadTask(failTempUploadTask);
                    uploadingProgress = 0;
                }
            }
        }
    }

    private void addMultiUploadStatusView(){
        if(taskUploadProgressFragment == null) {
            taskUploadProgressFragment = new TaskUploadProgressFragment();
            taskUploadProgressFragment.setTaskUploadEventListener(this);
        }
    }

    private void showMultiUploadStatusView(boolean isShowToCacheUpload){
        if(taskUploadProgressFragment != null){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if(taskUploadProgressFragment.isAdded()){
                taskUploadProgressFragment.setShowToCacheUpload(isShowToCacheUpload);
                fragmentTransaction.show(taskUploadProgressFragment);
            }else {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isShowToCacheUpload", isShowToCacheUpload);
                if(waitStepIndexs != null){
                    bundle.putStringArrayList("waitStepIndexs", waitStepIndexs);
                }
                if(uploadedStepIndexs != null){
                    bundle.putStringArrayList("uploadedStepIndexs", uploadedStepIndexs);
                }
                taskUploadProgressFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.rootView, taskUploadProgressFragment);
            }
            fragmentTransaction.commit();
        }
    }

    private void hideMultiUploadStatusView(){
        if(taskUploadProgressFragment != null){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(taskUploadProgressFragment);
            fragmentTransaction.commit();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startFloatingService(int recordStatus,int currentRecordTime) {
        if (floatRecordIntent == null) {
            floatRecordIntent = new Intent();
            floatRecordIntent.setAction("com.slicejobs.algsdk.algtasklibrary.FLOAT_RECORD_SERVICE");
            floatRecordIntent.setPackage(getPackageName());
        }
        floatRecordIntent.putExtra(CURRENT_RECORD_STATUS_KEY, recordStatus);
        floatRecordIntent.putExtra(CURRENT_RECORD_TIME_KEY, currentRecordTime);
        startService(floatRecordIntent);
    }

    private class HomeKeyReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    //表示按了home键,程序到了后台
                    if(algRecordFragment != null) {
                        if (isRecorder) {
                            if (Settings.canDrawOverlays(TaskStepsWebActivity.this)) {
                                startFloatingService(FloatingRecordService.FLOAT_REORD_STATUS_RECORDING,algRecordFragment.currentRecordTime);
                                ifHomePressed = true;
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onClose() {
        hideMultiUploadStatusView();
    }

    @Override
    public void onRetryClick(TempUploadTask failTempUploadTask) {
        if(tempUploadIntent == null){
            tempUploadIntent = new Intent(TaskStepsWebActivity.this, TaskResultUploadService.class);
        }
        tempUploadIntent.putExtra(TEMP_UPLOAD_MINITASK_KEY, tempUploadMiniTask);
        tempUploadIntent.putExtra(TEMP_UPLOAD_CACHEDIR_KEY, cacheDir);
        tempUploadIntent.putExtra(TEMP_UPLOAD_STEP_INDEX_KEY, failTempUploadTask.getStepIndex());
        String tempJsonKey = System.currentTimeMillis() + "";
        UPLOADJSONMAP.clear();
        UPLOADJSONMAP.put(tempJsonKey, failTempUploadTask.getResultJson());
        tempUploadIntent.putExtra(TEMP_UPLOAD_HASHMAP_JSON_KEY, tempJsonKey);
        tempUploadIntent.putExtra(TEMP_UPLOAD_ISCOMMIT_KEY, isCommit);
        startService(tempUploadIntent);

    }

    @Override
    public void onGoUploadCacheClick() {
        if (mWXSDKInstance != null) {
            Map<String, Object> tempUploadParams = new HashMap<>();
            tempUploadParams.put("eventType", "goUploadView");
            mWXSDKInstance.fireGlobalEventCallback("ossUploadJsonEvent", tempUploadParams);
        }

    }
}
