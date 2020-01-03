package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTask;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.model.SellersPhoto;
import com.slicejobs.algsdk.algtasklibrary.model.SellersRecord;
import com.slicejobs.algsdk.algtasklibrary.model.SellersVideo;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepResult;
import com.slicejobs.algsdk.algtasklibrary.model.Upload;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.Api;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.oss.OnUploadListener;
import com.slicejobs.algsdk.algtasklibrary.net.oss.OssUploader;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.UploadCacheAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.NetWorkUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by keller.zhou on 16/3/23.
 * 上传缓存
 */
public class UploadCacheActivity extends BaseActivity {
    public static final String EXTRA_TASK = "extra_task";
    public static final String TASK_RESULT = "current_task_result";
    public static final String TASK_LOCATION = "current_task_location";
    public static final String TASK_DURATION = "taskDuration";
    public static final String TASK_INTERRUPTED_TIMES = "interrupted_times";
    public static final String TASK_OUTRANGE_TIMES = "outrange_times";
    public static final String TASK_MARKET_INFO = "marketInfo";
    public static final int RESUNT_CODE_TASKDETAIL = 22222;//（任务详情进入）


    @BindView(R2.id.upload_task_list)
    RecyclerView uploadTaskList;
    @BindView(R2.id.btn_onkey_upload)
    Button btStartUpload;

    @BindView(R2.id.action_go_back)
    LinearLayout tvActionGoBack;

    @BindView(R2.id.tv_upload_title)
    TextView tvUploadTitle;
    @BindView(R2.id.uploadNotice)
    TextView uploadNotice;


    private MiniTask task;

    private UploadCacheAdapter uploadCacheAdapter;

    private Gson gson;

    private Map<String, TaskStepResult> taskStepResultMap = new HashMap<>();

    private List<MiniTaskStep> taskSteps;

    private List<TaskStepResult> resultList;

    private List<Upload> listTasks;//总任务

    private List<Upload> currentTasks = new ArrayList<>();//但前任务

    private int id= 1;

    private int currUploadNumber = 0;//正在上传的任务数

    private int currUploadMax = 0;//当前上传位置

    private int successProgress = 0;//上传成功总进度

    private int currentSuccessSum = 0;//当前任务成功数

    private boolean isStop = false;
    private String resultJson;

    private String location = "0,0";

    private String taskDuration = "0";


    private String interruptedTimes = "0";

    private String outrangeTimes = "0";

    private UploadTaskThread uploadTaskThread;

    private boolean isShowCommitDialog = false;

    private LocationClient locationClient;

    private String uploadErrMsg = "default";

    private User user;

    private String cacheUploadStatus = "0";//默认缓存上传

    private String marketGatherinfo = "";

    private boolean ifFirstCommit = true;//任务是否第一次提交

    private String mFinishLocationLon;

    private String mFinishLocationLat;

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0x10001) {
                btStartUpload.setText("再次上传");
                btStartUpload.setEnabled(true);
                btStartUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //用户触发再次上传
                        PrefUtil.make(UploadCacheActivity.this, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除token

                        btStartUpload.setText("正在上传");
                        btStartUpload.setEnabled(false);
                        List<Upload> list2 = multipleCreateUploadTask();
                        uploadCacheAdapter.setUploadTask(list2);
                        currentSuccessSum = 0;

                        currUploadNumber = 0;
                        currUploadMax = 0;
                        isStop = true;
                        uploadTaskThread = new UploadTaskThread();
                        uploadTaskThread.start();
                    }
                });
            } else if (msg.what == 0x10005) {
                /*try {
                    showCommitDialog(UploadCacheActivity.this);
                } catch (Exception e) {
                    try{
                        showCommitDialog(UploadCacheActivity.this.getParent());
                    } catch (Exception e1) {
                        toast("请重新提交");
                    }
                }*/
                UploadCacheActivity.this.showProgressDialog();
                //打包
                for (Upload upload : listTasks) {
                    if (resultJson.contains(upload.getPath())) {
                        resultJson = resultJson.replace(upload.getPath(), upload.getUrl());
                    }
                }

                startLocating(new BDAbstractLocationListener() {

                    @Override
                    public void onReceiveLocation(BDLocation bdLocation) {
                        locationClient.unRegisterLocationListener(this);
                        if (bdLocation != null) {
                            if (bdLocation.getLongitude() == 4.9E-324 || bdLocation.getLatitude() == 4.9E-324 || bdLocation.getLongitude() == 0 || bdLocation.getLatitude() == 0) {//还是没有得到经纬度,拿心跳
                                mFinishLocationLon = "0";
                                mFinishLocationLat = "0";
                                UploadCacheActivity.this.finishTask("0,0");
                            } else {
                                mFinishLocationLon = bdLocation.getLongitude() + "";
                                mFinishLocationLat = bdLocation.getLatitude() + "";
                                UploadCacheActivity.this.finishTask(bdLocation.getLongitude() + "," + bdLocation.getLatitude());
                            }
                        } else {
                            UploadCacheActivity.this.finishTask( "0,0");
                        }
                    }
                });
            } else {
                //从0开始
                int startTaskNumber = 1;//3 - msg.arg1;

                if (startTaskNumber == 1) {//需要开启一个任务
                    if (currUploadMax >= currentTasks.size()) {
                        return;
                    }
                    Upload upload = currentTasks.get(currUploadMax);
                    if (upload.getType().equals(TaskStep.EVIDENCETYPE_PHPTO)) {
                        startUpload(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                    } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                        startUploadVideo(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                    } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                        startUploadRecord(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                    }
                } else if (startTaskNumber == 2) {
                    for (int i = 0; i < 2; i++) {
                        if (currUploadMax >= currentTasks.size()) {
                            return;
                        }
                        Upload upload = currentTasks.get(currUploadMax);
                        if (upload.getType().equals(TaskStep.EVIDENCETYPE_PHPTO)) {
                            startUpload(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax );
                        } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                            startUploadVideo(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                        } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                            startUploadRecord(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                        }
                    }

                } else if (startTaskNumber == 3) {
                    for (int i = 0; i < 3; i++) {
                        if (currUploadMax >= currentTasks.size()) {
                            return;
                        }
                        Upload upload = currentTasks.get(currUploadMax);
                        if (upload.getType().equals(TaskStep.EVIDENCETYPE_PHPTO)) {
                            startUpload(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                        } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                            startUploadVideo(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                        } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                            startUploadRecord(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, currUploadMax);
                        }
                    }

                }
            }

            super.handleMessage(msg);
        }
    };


    /**
     * 任务详情进入
     * @param context
     * @param task
     * @return
     */
    public static Intent getStartIntent(Context context, Task task) {
        Intent intent = new Intent(context, UploadCacheActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        return intent;
    }


    //兼容旧的报错，以过期
    public static Intent getStartIntent(Context context, Task task, String resultJson, String loca) {
        Intent intent = new Intent(context, UploadCacheActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        intent.putExtra(TASK_RESULT, resultJson);
        intent.putExtra(TASK_LOCATION, loca);
        return intent;
    }


    /**
     * 分布 最后上传 进入
     * @param context
     * @param loca
     * @return
     */
    public static Intent getStartIntent(Context context, MiniTask miniTask, String resultJson, String loca, String taskDuration, String interruptedTimes, String outrangeTimes) {
        Intent intent = new Intent(context, UploadCacheActivity.class);
        intent.putExtra(EXTRA_TASK, miniTask);
        intent.putExtra(TASK_RESULT, resultJson);
        intent.putExtra(TASK_LOCATION, loca);
        intent.putExtra(TASK_DURATION, taskDuration);
        intent.putExtra(TASK_INTERRUPTED_TIMES, interruptedTimes);
        intent.putExtra(TASK_OUTRANGE_TIMES, outrangeTimes);
        return intent;
    }

    /**
     * 分布 最后上传 进入
     * @param context
     * @param task
     * @param loca
     * @return
     */
    public static Intent getStartIntent(Context context, MiniTask task, String resultJson, String loca, String taskDuration, String interruptedTimes, String outrangeTimes, String marketInfo) {
        Intent intent = new Intent(context, UploadCacheActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        intent.putExtra(TASK_RESULT, resultJson);
        intent.putExtra(TASK_LOCATION, loca);
        intent.putExtra(TASK_DURATION, taskDuration);
        intent.putExtra(TASK_INTERRUPTED_TIMES, interruptedTimes);
        intent.putExtra(TASK_OUTRANGE_TIMES, outrangeTimes);
        intent.putExtra(TASK_MARKET_INFO, marketInfo);
        return intent;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_taskcache);
        PrefUtil.make(UploadCacheActivity.this, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
        ButterKnife.bind(this);
        initWedgets();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initWedgets() {
        task = (MiniTask) getIntent().getSerializableExtra(EXTRA_TASK);
        user = BizLogic.getCurrentUser();

        ifFirstCommit = task.isIfFirstCommit();
        taskSteps = task.getTaskSteps();
        location = getIntent().getStringExtra(TASK_LOCATION);
        resultJson = getIntent().getStringExtra(TASK_RESULT);
        taskDuration = getIntent().getStringExtra(TASK_DURATION);

        interruptedTimes = getIntent().getStringExtra(TASK_INTERRUPTED_TIMES);
        outrangeTimes = getIntent().getStringExtra(TASK_OUTRANGE_TIMES);

        marketGatherinfo = getIntent().getStringExtra(TASK_MARKET_INFO);

        if (StringUtil.isNotBlank(resultJson)) {
            gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
            resultList = gson.fromJson(
                    resultJson, new TypeToken<List<TaskStepResult>>() {
                    }.getType());

            for (int i = 0; i < resultList.size(); i++) {
                TaskStepResult result = resultList.get(i);
                taskStepResultMap.put(result.getStepId(), result);
            }
        }


        uploadCacheAdapter = new UploadCacheAdapter(new MyItemClickListener(), uploadTaskList);
        uploadTaskList.setAdapter(uploadCacheAdapter);
        uploadTaskList.setLayoutManager(new LinearLayoutManager(this));
        uploadTaskList.setItemAnimator(new DefaultItemAnimator());

        listTasks = createUploadTask();
        //删除本地路径相同的upload
        for (int i = 0; i < listTasks.size()-1; i++) {
            for (int j = listTasks.size()-1; j > i; j--) {
                if (listTasks.get(j).getPath().equals(listTasks.get(i).getPath())) {
                    listTasks.remove(j);
                }
            }
        }
        currentTasks.addAll(listTasks);


        //创建上传任务
        uploadCacheAdapter.setUploadTask(currentTasks);


        tvUploadTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (StringUtil.isNotBlank(uploadErrMsg)) {
                    showHintDialog(new DialogDefineClick() {
                        @Override
                        public void defineClick() {

                        }
                    }, "证据上传失败的具体原因", uploadErrMsg, "我知道了", false);
                }
                return false;
            }
        });


    }



    /**
     * 首次创建上传任务
     * @return
     */
    public List<Upload> createUploadTask() {
        List<Upload> list = new ArrayList<>();
        if (taskSteps != null) {
            for (int i = 0;i<taskSteps.size();i++) {
                MiniTaskStep taskStep = taskSteps.get(i);
                if(taskStep != null){
                    TaskStepResult taskStepResult =  taskStepResultMap.get(taskStep.getStepId());
                    if (taskStepResult == null) {
                        continue;
                    }

                    if (taskStep.getEvidenceType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                        List<SellersVideo> videoList = taskStepResult.getVideoList();
                        List<ResultVideo> videos = taskStepResult.getVideos();
                        for (ResultVideo resultVideo : videos) {
                            if (!resultVideo.getThumbUrl().contains("http://")) {
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_PHPTO);
                                upload.setPath(resultVideo.getThumbUrl());
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setFirstCommit(ifFirstCommit);
                                for (SellersVideo sellersVideo : videoList) {
                                    if(StringUtil.isNotBlank(sellersVideo.getThumbLocalPath())) {
                                        if (resultVideo.getThumbUrl().equals(sellersVideo.getThumbLocalPath()) && sellersVideo.isReplenish()) {
                                            upload.setCache(true);
                                        }
                                    }
                                }
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }
                            }
                            if (!resultVideo.getVideoUrl().contains("http://")) {
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_VIDEO);
                                upload.setPath(resultVideo.getVideoUrl());
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setFirstCommit(ifFirstCommit);
                                for (SellersVideo sellersVideo : videoList) {
                                    if(StringUtil.isNotBlank(sellersVideo.getLocalPath())) {
                                        if (resultVideo.getVideoUrl().equals(sellersVideo.getLocalPath()) && sellersVideo.isReplenish()) {
                                            upload.setCache(true);
                                        }
                                    }
                                }
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }

                            }
                        }

                    } else if (taskStep.getEvidenceType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                        List<String> records = taskStepResult.getRecords();
                        for (String str : records) {
                            if (!str.contains("http://")) {
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_RECORD);
                                upload.setPath(str);
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setFirstCommit(ifFirstCommit);
                                for (SellersRecord sellersEvidence : taskStepResult.getRecordList()) {
                                    if(StringUtil.isNotBlank(sellersEvidence.getLocalPath())) {
                                        if(str.equals(sellersEvidence.getLocalPath()) && sellersEvidence.isReplenish()){
                                            upload.setCache(true);
                                        }
                                    }
                                }
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }
                            }
                        }

                    } else {
                        List<String> photos = taskStepResult.getPhotos();
                        for (String str : photos) {
                            if (!str.contains("http://")) {
                                if (str.contains("Thumbnail")) {//照片有缩略图，替换原文件

                                    String source = str.replace("/Thumbnail", "/Photo");
                                    resultJson = resultJson.replace(str, source);//替换json
                                    str = source;
                                }
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_PHPTO);
                                upload.setPath(str);
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setFirstCommit(ifFirstCommit);
                                for (SellersPhoto sellersEvidence : taskStepResult.getPhotoList()) {
                                    if(StringUtil.isNotBlank(sellersEvidence.getLocalPath())){
                                        if(str.equals(sellersEvidence.getLocalPath()) && sellersEvidence.isReplenish()){
                                            upload.setCache(true);
                                        }
                                    }
                                }
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }
                            }
                        }
                    }
                }

            }

        }

        if (null != list && list.size() == 0) {//没有上传任务，显示返回键
            tvActionGoBack.setVisibility(View.VISIBLE);
        }

        return list;
    }


    /**
     * 二次创建上传任务
     */
    public List<Upload> multipleCreateUploadTask() {
        currentTasks.clear();
        for (Upload upload : listTasks) {
            if (upload.getUrl() == null) {
                currentTasks.add(upload);
            }
        }
        return currentTasks;
    }


    @OnClick({R2.id.btn_onkey_upload, R2.id.action_go_back})
    public void OnClick(View view) {
        if (view.getId() == R.id.btn_onkey_upload) {
            //开启线程，自动上传，每次都上传3个任务
            btStartUpload.setText("正在上传");
            btStartUpload.setEnabled(false);
            isStop = true;
            uploadTaskThread = new UploadTaskThread();
            uploadTaskThread.start();
            if(NetWorkUtil.isWifiActive()){
            }else {
            }
        } else if (view.getId() == R.id.action_go_back) {
            backDown();
        }

    }


    private void startUpload(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload, int currUploadIndex) {
        currUploadNumber ++;
        currUploadMax ++;

        new OssUploader(SliceApp.CONTEXT).uploadTaskCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getOrderid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                currUploadNumber--;
                successProgress++;
                currentSuccessSum++;
                showProgress();
                uploadCacheAdapter.updateTaskStatus(filePath, 101, currUploadIndex);

                for (Upload upload1 : listTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                //upload.setUrl(url);
                checkUploadFinlish();
            }

            @Override
            public void onUploadFail(String msg) {
                if (!uploadErrMsg.equals(msg)) {
                    uploadErrMsg = msg;
                    if (uploadErrMsg.contains("Unable to resolve host")) {//网络问题
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }else if (uploadErrMsg.contains("Can't get a federation token")) {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("登录信息验证失败，请重新登录");
                    }else {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }
                }

                try {
                    currUploadNumber--;
                    uploadCacheAdapter.updateTaskStatus(filePath, -1, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }

            }

            @Override
            public void onUploadProgress(int percent) {
                try {

                    uploadCacheAdapter.updateTaskStatus(filePath, percent, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }
            }
        });

    }

    private void startUploadVideo(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload, int currUploadIndex) {
        currUploadNumber ++;
        currUploadMax ++;

        new OssUploader(SliceApp.CONTEXT).uploadTaskVideoCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getTaskid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                currUploadNumber--;
                successProgress++;
                currentSuccessSum++;
                showProgress();
                uploadCacheAdapter.updateTaskStatus(filePath, 101, currUploadIndex);
                for (Upload upload1 : listTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                //upload.setUrl(url);
                checkUploadFinlish();
            }


            @Override
            public void onUploadFail(String msg) {
                if (!uploadErrMsg.equals(msg)) {
                    uploadErrMsg = msg;
                    if (uploadErrMsg.contains("Unable to resolve host")) {//网络问题
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }else if (uploadErrMsg.contains("Can't get a federation token")) {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("登录信息验证失败，请重新登录");
                    }else {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }
                }


                try {
                    currUploadNumber--;
                    uploadCacheAdapter.updateTaskStatus(filePath, -1, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }

            }

            @Override
            public void onUploadProgress(int percent) {
                try {
                    uploadCacheAdapter.updateTaskStatus(filePath, percent, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }
            }
        });

    }

    private void startUploadRecord(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload, int currUploadIndex) {
        currUploadNumber ++;
        currUploadMax ++;

        new OssUploader(SliceApp.CONTEXT).uploadTaskRecordCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getTaskid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                currUploadNumber--;
                successProgress++;
                currentSuccessSum++;
                //更新进度显示
                showProgress();
                uploadCacheAdapter.updateTaskStatus(filePath, 101, currUploadIndex);
                for (Upload upload1 : listTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                checkUploadFinlish();
            }

            @Override
            public void onUploadFail(String msg) {
                if (!uploadErrMsg.equals(msg)) {
                    uploadErrMsg = msg;
                    if (uploadErrMsg.contains("Unable to resolve host")) {//网络问题
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }else if (uploadErrMsg.contains("Can't get a federation token")) {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("登录信息验证失败，请重新登录");
                    }else {
                        uploadNotice.setTextColor(Color.RED);
                        uploadNotice.setText("当前网络信号不稳定，请稍后重试");
                    }
                }
                try {
                    currUploadNumber--;
                    uploadCacheAdapter.updateTaskStatus(filePath, -1, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }
            }

            @Override
            public void onUploadProgress(int percent) {
                try {
                    uploadCacheAdapter.updateTaskStatus(filePath, percent, currUploadIndex);
                } catch (Exception e) {
                    // eat exception
                }
            }
        });
    }



    class UploadTaskThread  extends Thread {

        @Override
        public void run() {

            while (isStop) {//每过一秒检测任务数量
                try {
                    if (currUploadNumber < 1) {//3
                        if (currUploadMax >= currentTasks.size()) {
                            isStop = false;
                            uploadTaskThread = null;
                            if (currentSuccessSum < currentTasks.size()) {//检测到上传状态出现异常时候
                                Message message = Message.obtain();
                                message.what = 0x10001;
                                handler.sendMessage(message);
                            }

                            return;
                        }
                        Message message = Message.obtain();
                        message.what = 0x10002;
                        message.arg1 = currUploadNumber;
                        handler.sendMessage(message);
                    }

                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            super.run();
        }
    }


    /**
     * 检测是否上传完成
     * @return
     */
    public void checkUploadFinlish() {
        boolean flag = true;
        for (Upload upload1 : listTasks) {
            if (StringUtil.isBlank(upload1.getUrl())) {
                flag = false;
            }
        }

        if (flag) {
            //打包
            uploadFinlishDialog();
        }
    }


    /**
     * 上传完成弹出对话框，提交结果
     */
    public void uploadFinlishDialog() {
        btStartUpload.setEnabled(true);
        btStartUpload.setText("提交结果");
        btStartUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showCommitDialog(UploadCacheActivity.this);
                } catch (Exception e) {
                    try {
                        showCommitDialog(UploadCacheActivity.this.getParent());
                    } catch (Exception e1) {
                        toast("请重新提交");
                    }
                }
            }
        });


        if (!isShowCommitDialog) { //不能在其他线程弹出dialog
            Message msg = Message.obtain();
            msg.what = 0x10005;
            handler.sendMessage(msg);
        }
    }


    public void showCommitDialog(Context context) throws Exception {
        isShowCommitDialog = true;
        AlertDialog.Builder builer = new  AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        tvTitle.setText(getString(R.string.text_slicejobs_hint));
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(getString(R.string.text_cache_finlish));
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        tvBt.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
        linearLayout.setVisibility(View.VISIBLE);
        Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
        btCancel.setText(getString(R.string.cancel));
        Button btDefine = (Button) view.findViewById(R.id.dialog_define);
        btDefine.setText(getString(R.string.hint_submit));
        builer.setCancelable(false);
        builer.setView(view);
        AlertDialog dialog = builer.create();
        dialog.show();

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击取消按钮,调用接口
                dialog.dismiss();
                isShowCommitDialog = false;
                toast("请尽快完成任务");
            }
        });
        btDefine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击确定按钮,调用接口
                dialog.dismiss();
                isShowCommitDialog = false;
                UploadCacheActivity.this.showProgressDialog();
                //打包
                for (Upload upload : listTasks) {
                    if (resultJson.contains(upload.getPath())) {
                        resultJson = resultJson.replace(upload.getPath(), upload.getUrl());
                    }
                }

                startLocating(new BDAbstractLocationListener() {

                    @Override
                    public void onReceiveLocation(BDLocation bdLocation) {
                        locationClient.unRegisterLocationListener(this);
                        if (bdLocation != null) {
                            if (bdLocation.getLongitude() == 4.9E-324 || bdLocation.getLatitude() == 4.9E-324 || bdLocation.getLongitude() == 0 || bdLocation.getLatitude() == 0) {//还是没有得到经纬度,拿心跳
                                mFinishLocationLon = "0";
                                mFinishLocationLat = "0";
                                UploadCacheActivity.this.finishTask("0,0");
                            } else {
                                mFinishLocationLon = bdLocation.getLongitude() + "";
                                mFinishLocationLat = bdLocation.getLatitude() + "";
                                UploadCacheActivity.this.finishTask(bdLocation.getLongitude() + "," + bdLocation.getLatitude());
                            }
                        } else {
                            UploadCacheActivity.this.finishTask( "0,0");
                        }
                    }
                });
            }
        });
        Window dialogWindow = dialog.getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }




    private void finishTask( String finishLocation) {
        //检测文件是否全部存在
        int isHaveEvidenceIndex = checkHaveEvidence();
        if (isHaveEvidenceIndex != -1) {
            toast("您第"+(isHaveEvidenceIndex+1)+"步骤，有照片找不到了，可能被删除了，请点击左上角返回，检查证据是否丢失。");
            return;
        }

        if (StringUtil.isNotBlank(task.getRfversion()) && task.getRfversion().equals("1")) {
            newFinishTask(finishLocation);
            Log.d("-----------------", "newFinishTask");
        } else {
            Log.d("-----------------", "olaFinishTask");
            olaFinishTask(finishLocation);
        }
    }



    private int checkHaveEvidence() {//检查本地有文件丢失情况
        int isHaveEvidence = -1;
        if (null != gson) {
            List<TaskStepResult> resultList = gson.fromJson(resultJson, new TypeToken<List<TaskStepResult>>() {}.getType());
            for (int index = 0; index < resultList.size(); index ++) {
                TaskStepResult taskStepResult = resultList.get(index);
                if (taskStepResult != null && taskStepResult.getPhotos().size() > 0) {
                    for (String photo : taskStepResult.getPhotos()) {
                        if (!photo.startsWith("http")) {
                            isHaveEvidence = index;
                        }
                    }
                } else if (taskStepResult != null && taskStepResult.getRecords().size() > 0) {
                    for (String record : taskStepResult.getRecords()) {
                        if (!record.startsWith("http")) {
                            isHaveEvidence = index;
                        }
                    }
                } else if (taskStepResult != null && taskStepResult.getVideos().size() > 0) {
                    for (ResultVideo video : taskStepResult.getVideos()) {
                        if (!video.getVideoUrl().startsWith("http")) {
                            isHaveEvidence = index;
                        }
                    }
                }
            }
        }

        return isHaveEvidence;

    }



    private void olaFinishTask(String finishLocation) {
        String timestamp = DateUtil.getCurrentTime();
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder signBuilder = new SignUtil.SignBuilder();
        signBuilder.put("userid", user.userid)
                .put("taskid", task.getTaskid())
                .put("templateresultjson", resultJson)
                .put("cacheuploadstatus", cacheUploadStatus)
                .put("op", "finish");
        if (location != null) {
            signBuilder.put("checkinlocation", location);
        } else {
            signBuilder.put("checkinlocation", "0,0");
        }
        signBuilder.put("location", finishLocation);
        signBuilder.put("timestamp", timestamp);
        signBuilder.put("sec_consumed", taskDuration);
        signBuilder.put("interrupted_times", interruptedTimes);
        signBuilder.put("outrange_times", outrangeTimes);
        signBuilder.put("appId", appId);



        String sig = signBuilder.build();
        Api api = RestClient.getInstance().provideApi();
        Observable<Response<Task>> taskObservable = null;
        if (location == null) {
            taskObservable = api.updateCacheTemplateTaskStatus(user.userid, "finish", task.getTaskid(),
                    resultJson, finishLocation, "0,0", cacheUploadStatus, timestamp, taskDuration,interruptedTimes, outrangeTimes,appId,sig);
        } else {
            taskObservable = api.updateCacheTemplateTaskStatus(user.userid, "finish", task.getTaskid(),
                    resultJson, finishLocation, location, cacheUploadStatus, timestamp, taskDuration,interruptedTimes, outrangeTimes,appId, sig);
        }
        taskObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Response<Task>>() {
                    @Override
                    public void call(Response<Task> res) {
                        UploadCacheActivity.this.dismissProgressDialog();
                        if (res.ret == 0) {

                            BusProvider.getInstance().post(new AppEvent.TaskStatusEvent(task.getTaskid(), "4"));
                            UploadCacheActivity.this.setResult(RESULT_OK);
                            UploadCacheActivity.this.toast(getString(R.string.upload_success));
                            UploadCacheActivity.this.finish();
                            UploadCacheActivity.this.deleteTaskCache();
                        } else {
                            UploadCacheActivity.this.toast(res.msg);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        toast("任务上传失败，请检查当前网络环境是否正常");
                        UploadCacheActivity.this.dismissProgressDialog();
                    }
                });

    }


    private void newFinishTask( String finishLocation) {
        Log.d("----------------", "newFinishTask"+task.getOrderid()+"------"+user.userid);
        String timestamp = DateUtil.getCurrentTime();
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder signBuilder = new SignUtil.SignBuilder();
        signBuilder.put("userid", user.userid)
                .put("orderid", task.getOrderid())
                .put("templateresultjson", resultJson)
                .put("cacheuploadstatus", cacheUploadStatus)
                .put("op", "finish");
        if (location != null) {
            signBuilder.put("checkinlocation", location);
        } else {
            signBuilder.put("checkinlocation", "0,0");
        }
        signBuilder.put("location", finishLocation);
        signBuilder.put("timestamp", timestamp);
        signBuilder.put("sec_consumed", taskDuration);
        signBuilder.put("interrupted_times", interruptedTimes);
        signBuilder.put("outrange_times", outrangeTimes);
        signBuilder.put("appId", appId);
        if(StringUtil.isNotBlank(marketGatherinfo)) {
            signBuilder.put("market_gatherinfo", marketGatherinfo);
        }
        String sig = signBuilder.build();
        Api api = RestClient.getInstance().provideApi();
        Observable<Response<Task>> taskObservable = null;
        if (location == null) {
            if(StringUtil.isNotBlank(marketGatherinfo)) {
                taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                        resultJson, finishLocation, "0,0", cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes, marketGatherinfo,appId, sig);
            }else {
                taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                        resultJson, finishLocation, "0,0", cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes,appId, sig);
            }
        } else {
            if(StringUtil.isNotBlank(marketGatherinfo)) {
                taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                        resultJson, finishLocation, location, cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes, marketGatherinfo, appId,sig);
            }else {
                taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                        resultJson, finishLocation, location, cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes,appId, sig);
            }
        }
        taskObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    UploadCacheActivity.this.dismissProgressDialog();
                    if (res.ret == 0) {
                        BusProvider.getInstance().post(new AppEvent.TaskStatusEvent(task.getTaskid(), "4"));
                        setResult(RESULT_OK);
                        toast(getString(R.string.upload_success));
                        finish();
                        deleteTaskCache();
                    } else if (res.ret == 345) {//验证地理位置失败，请在任务范围内提交
                        if(StringUtil.isNotBlank(mFinishLocationLon) && mFinishLocationLon.equals("0") &&
                                StringUtil.isNotBlank(mFinishLocationLat) && mFinishLocationLat.equals("0")){
                            showHintDialog(new DialogDefineClick() {
                                @Override
                                public void defineClick() {

                                }
                            }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "当前位置GPS信号弱，请重试提交", "我知道了", false);
                        }else {

                        }
                    }else {
                        toast(res.msg);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        toast("任务上传失败，请检查当前网络环境是否正常");
                        UploadCacheActivity.this.dismissProgressDialog();
                    }
                });

    }




    /**
     * 显示上传的总进度和进度条变化情况
     */
    private void showProgress() {
        String str = successProgress +"/"+listTasks.size();

    }



    class MyItemClickListener implements UploadCacheAdapter.ItemClickListener{


        @Override
        public void onItemUploadButtonClick(int index) {//再次上传这个任务
            PrefUtil.make(UploadCacheActivity.this, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除token

            if (index >= currentTasks.size()) {//防止IndexOutOfBoundsException
                return;
            }

            Upload upload = currentTasks.get(index);
            if (upload.getType().equals(TaskStep.EVIDENCETYPE_PHPTO)) {
                startUpload(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, index);
            } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                startUploadVideo(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, index);
            } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                startUploadRecord(upload.getStepIndex(),upload.getStepId(),upload.isFirstCommit(),upload.isCache(),upload.getPath(), upload, index);
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        backDown();
        return false;
    }


    public void backDown() {
        if (isStop && successProgress != currentTasks.size()) {//正在上传过程中
            showHintDialog(new DialogClickLinear() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void defineClick() {
                    isStop = false;//关闭任务
                    uploadTaskThread = null;
                    UploadCacheActivity.this.finish();
                }
            }, getString(R.string.text_slicejobs_hint), "正在上传，确定退出吗？", getString(R.string.cancel), getString(R.string.confirm), false);
        } else {
            UploadCacheActivity.this.finish();
        }
    }

    private void startLocating(BDAbstractLocationListener listener) {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.addrType = "all";
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(listener);
        locationClient.start();
    }

    public void deleteTaskCache() {
        User user = BizLogic.getCurrentUser();

        String tempFile = AppConfig.TEMP_CACHE_DIR + File.separator + user.userid + "-" + task.getTaskid();

        String cacheFile = AppConfig.LONG_CACHE_DIR + File.separator + user.userid + "-" + task.getTaskid();

        final File dir;

        File dir1 = new File(tempFile);
        File dir2 = new File(cacheFile);
        if (dir1.exists()) {
            dir = dir1;
        } else {
            dir = dir2;
        }

        if (dir.exists()) {//如果有缓存目录,结束时间有效
            Observable.create(subscriber -> {
                try {
                    if (dir.isDirectory()) {
                        for (File f : dir.listFiles()) {
                            if (f.isDirectory()) {
                                for (File f2 : f.listFiles()) {
                                    f2.delete();
                                }
                            }
                            f.delete();
                        }
                        dir.delete();
                    }
                } catch (Exception e) {
                    subscriber.onNext(null);
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }


}
