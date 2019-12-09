package com.slicejobs.algsdk.algtasklibrary.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTask;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.model.SellersPhoto;
import com.slicejobs.algsdk.algtasklibrary.model.SellersRecord;
import com.slicejobs.algsdk.algtasklibrary.model.SellersVideo;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepResult;
import com.slicejobs.algsdk.algtasklibrary.model.TempUploadTask;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.net.UploadTaskStepResultTask;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskStepsWebActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskResultUploadService extends Service {

    public static final String ACTION_MULTI_UPLOAD_LINES_STATUS = "com.slicejobs.ailinggong.ACTION_MULTI_UPLOAD_LINES_STATUS";
    public static final String MULTI_UPLOAD_CURRENT_INDEX_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_CURRENT_INDEX_KEY";
    public static final String MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY";
    public static final String MULTI_UPLOAD_CURRENT_LINE_FINISH_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_CURRENT_LINE_FINISH_KEY";
    public static final String MULTI_UPLOAD_IS_UPDATE_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_IS_UPDATE_KEY";
    public static final String MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY";
    public static final String MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY";
    public static final String ACTION_MULTI_UPLOAD_ERROR = "com.slicejobs.ailinggong.ACTION_MULTI_UPLOAD_ERROR";
    public static final String MULTI_UPLOAD_ERROR_JSON_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_ERROR_JSON_KEY";
    public static final String MULTI_UPLOAD_UPLOADED_JSON_MAP_KEY = "com.slicejobs.ailinggong.MULTI_UPLOAD_UPLOADED_JSON_MAP_KEY";
    private MiniTask miniTask;
    private String cacheDir;
    private boolean isCommit;//是否是最后一步该提交了
    private UploadTaskStepResultTask uploadTaskStepResultTask;
    private TempUploadTask uploadFinishTask;
    private TempUploadTask uploadingTask;
    private TempUploadTask waitUploadTask;
    private Gson gson;
    private ArrayList<String> uploadingStepIndexList;
    private ArrayList<String> waitStepIndexList;
    private ArrayList<String> uploadedStepIndexList;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(UploadTaskStepResultTask.ACTION_UPLOAD_PROGRESS);
        registerReceiver(uploadProgressReceiver, intentFilter);
        uploadingStepIndexList = new ArrayList<>();
        waitStepIndexList = new ArrayList<>();
        uploadedStepIndexList = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(miniTask == null){
                miniTask = (MiniTask) intent.getSerializableExtra(TaskStepsWebActivity.TEMP_UPLOAD_MINITASK_KEY);
            }
            if(cacheDir == null){
                cacheDir = intent.getStringExtra(TaskStepsWebActivity.TEMP_UPLOAD_CACHEDIR_KEY);
            }
            isCommit = intent.getBooleanExtra(TaskStepsWebActivity.TEMP_UPLOAD_ISCOMMIT_KEY, false);
            String uploadStatus = intent.getStringExtra(TaskStepsWebActivity.TEMP_UPLOAD_STATUS_KEY);
            String tempResultJsonKey = intent.getStringExtra(TaskStepsWebActivity.TEMP_UPLOAD_HASHMAP_JSON_KEY);
            String stepIndex = intent.getStringExtra(TaskStepsWebActivity.TEMP_UPLOAD_STEP_INDEX_KEY);
            String tempResultJson = null;
            if(StringUtil.isNotBlank(tempResultJsonKey)){
                tempResultJson = TaskStepsWebActivity.UPLOADJSONMAP.get(tempResultJsonKey);
            }
            if(StringUtil.isNotBlank(uploadStatus) && StringUtil.isNotBlank(tempResultJson)) {
                if (uploadStatus.equals("finish")) {
                    uploadFinishTask = new TempUploadTask(stepIndex,tempResultJson);
                    uploadedStepIndexList.add(0,stepIndex);
                    Intent multiLinesStatusIntent = new Intent(ACTION_MULTI_UPLOAD_LINES_STATUS);
                    multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_INDEX_KEY, stepIndex);
                    multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY, 100);
                    multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY, uploadedStepIndexList);
                    multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY, waitStepIndexList);
                    sendBroadcast(multiLinesStatusIntent);
                } else if (uploadStatus.equals("wait")){
                    if (uploadingTask == null) {//如果没有正在上传的 开始上传
                        if(uploadFinishTask != null){
                            uploadingTask = getWaitUploadTask(stepIndex,tempResultJson);
                        }else {
                            uploadingTask = new TempUploadTask(stepIndex,tempResultJson);
                        }
                        if(uploadedStepIndexList.contains(stepIndex)){//已经上传过这一题
                            uploadingTask.isUpdate = true;//再上传就是更新
                        }else {
                            uploadingTask.isUpdate = false;
                        }
                        uploadTaskStepResultTask = new UploadTaskStepResultTask(this, miniTask, uploadingTask.getResultJson(), cacheDir);
                        uploadTaskStepResultTask.execute();
                        uploadTaskStepResultTask.setUploadTaskStepResultListener(uploadResultListener);
                    } else {//有正在上传的，看是否有上传完成的，没有上传完成的直接添加在后面，有上传完成的，去重后添加在后面
                        if(!waitStepIndexList.contains(stepIndex)) {
                            waitStepIndexList.add(stepIndex);
                        }
                        if (uploadFinishTask == null) {
                            waitUploadTask = new TempUploadTask(stepIndex,tempResultJson);
                        } else {
                            waitUploadTask = getWaitUploadTask(stepIndex,tempResultJson);
                        }
                    }
                    if(uploadingStepIndexList.size() == 0) {
                        uploadingStepIndexList.add(0, stepIndex);
                    }else {
                        if(!uploadingStepIndexList.get(0).equals(stepIndex)){
                            uploadingStepIndexList.add(0, stepIndex);
                        }
                    }
                    Intent multiLinesStatusIntent = new Intent(ACTION_MULTI_UPLOAD_LINES_STATUS);
                    multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_INDEX_KEY, uploadingTask.getStepIndex());
                    multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY, 0);
                    multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY, uploadedStepIndexList);
                    multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY, waitStepIndexList);
                    sendBroadcast(multiLinesStatusIntent);
                }else if(uploadStatus.equals("exit")){//退出画面时关闭上传任务
                    if(uploadTaskStepResultTask != null) {
                        uploadTaskStepResultTask.setCacel(true);
                        uploadTaskStepResultTask.setUploadTaskStepResultListener(null);
                        uploadTaskStepResultTask.cancel(true);
                        uploadTaskStepResultTask = null;
                        System.gc();
                    }
                    if(uploadProgressReceiver != null) {
                        unregisterReceiver(uploadProgressReceiver);
                    }
                    if(uploadedStepIndexList != null){
                        uploadedStepIndexList.clear();
                    }
                    if(waitStepIndexList != null){
                        waitStepIndexList.clear();
                    }
                    if(uploadingStepIndexList != null){
                        uploadingStepIndexList.clear();
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private UploadTaskStepResultTask.UploadTaskStepResultListener uploadResultListener = new UploadTaskStepResultTask.UploadTaskStepResultListener() {
        @Override
        public void onUploadFinished(String result) {
            if(uploadTaskStepResultTask != null) {
                uploadTaskStepResultTask.cancel(true);
                uploadTaskStepResultTask = null;
                System.gc();
            }
            uploadingTask.setResultJson(result);
            uploadFinishTask = uploadingTask;
            uploadingTask = null;

            if(waitUploadTask != null){//上传完毕后是否有等待上传的，有，上传
                uploadingTask = waitUploadTask;
                waitUploadTask = null;
                if(uploadedStepIndexList.contains(uploadingTask.getStepIndex())){//已经上传过这一题
                    uploadingTask.isUpdate = true;//再上传就是更新
                }else {
                    uploadingTask.isUpdate = false;
                }
                uploadTaskStepResultTask = new UploadTaskStepResultTask(TaskResultUploadService.this,miniTask,uploadingTask.getResultJson(),cacheDir);
                uploadTaskStepResultTask.execute();
                uploadTaskStepResultTask.setUploadTaskStepResultListener(uploadResultListener);
                if(waitStepIndexList.contains(uploadingTask.getStepIndex())){
                    waitStepIndexList.remove(uploadingTask.getStepIndex());
                }
            }else {
                waitStepIndexList.clear();
                uploadedStepIndexList.addAll(0, uploadingStepIndexList);
                uploadingStepIndexList.clear();
            }

            Intent multiLinesStatusIntent = new Intent(ACTION_MULTI_UPLOAD_LINES_STATUS);
            multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_INDEX_KEY, uploadFinishTask.getStepIndex());
            multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY, 100);
            multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_LINE_FINISH_KEY, true);
            if(isCommit) {
                String tempJsonKey = System.currentTimeMillis() + "";
                TaskStepsWebActivity.UPLOADJSONMAP.clear();
                TaskStepsWebActivity.UPLOADJSONMAP.put(tempJsonKey, uploadFinishTask.getResultJson());
                multiLinesStatusIntent.putExtra(MULTI_UPLOAD_UPLOADED_JSON_MAP_KEY, tempJsonKey);
            }
            multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY, uploadedStepIndexList);
            multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY, waitStepIndexList);
            sendBroadcast(multiLinesStatusIntent);
        }

        @Override
        public void onUploadError() {
            if(uploadTaskStepResultTask != null) {
                uploadTaskStepResultTask.cancel(true);
                uploadTaskStepResultTask = null;
                System.gc();
            }
            Intent multiErrorIntent = new Intent(ACTION_MULTI_UPLOAD_ERROR);
            multiErrorIntent.putExtra(MULTI_UPLOAD_ERROR_JSON_KEY, uploadingTask);
            sendBroadcast(multiErrorIntent);
            if(uploadingTask != null) {
                if(uploadingStepIndexList.size() == 0) {
                    uploadingStepIndexList.add(0, uploadingTask.getStepIndex());
                }else {
                    if(!uploadingStepIndexList.get(0).equals(uploadingTask.getStepIndex())){
                        uploadingStepIndexList.add(0, uploadingTask.getStepIndex());
                    }
                }
            }
            uploadingTask = null;
        }
    };

    //把新添加进去的resultJson和已上传完毕的resultJson进行合并
    private TempUploadTask getWaitUploadTask(String stepIndex, String tempResultJson){
        gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        List<TaskStepResult> waitResultList = gson.fromJson(
                tempResultJson, new TypeToken<List<TaskStepResult>>() {
                }.getType());
        List<TaskStepResult> uploadedResultList = gson.fromJson(
                uploadFinishTask.getResultJson(), new TypeToken<List<TaskStepResult>>() {
                }.getType());
        for (int i = 0; i < waitResultList.size(); i++) {
            TaskStepResult waitTaskStepResult = waitResultList.get(i);
            for (int j = 0; j < uploadedResultList.size(); j++) {
                TaskStepResult uploadedTaskStepResult = uploadedResultList.get(j);
                if(waitTaskStepResult.getStepId().equals(uploadedTaskStepResult.getStepId())){
                    List<SellersPhoto> uploadSellersPhotos = uploadedTaskStepResult.getPhotoList();
                    List<SellersPhoto> waitSellersPhotos = waitTaskStepResult.getPhotoList();
                    if(waitSellersPhotos != null && waitSellersPhotos.size() != 0){
                        for (int k = 0; k < waitSellersPhotos.size(); k++) {
                            SellersPhoto waitSellerPhoto = waitSellersPhotos.get(k);
                            for (int l = 0; l < uploadSellersPhotos.size(); l++) {
                                SellersPhoto uploadedSellerPhoto = uploadSellersPhotos.get(l);
                                if(StringUtil.isNotBlank(waitSellerPhoto.getLocalPath()) &&
                                        StringUtil.isNotBlank(uploadedSellerPhoto.getLocalPath())) {
                                    if (waitSellerPhoto.getLocalPath().equals(uploadedSellerPhoto.getLocalPath())) {
                                        if (uploadedSellerPhoto.getPhotoUrl().startsWith("http")) {
                                            waitSellerPhoto.setPhotoUrl(uploadedSellerPhoto.getPhotoUrl());
                                            List<String> waitPhotos = waitTaskStepResult.getPhotos();
                                            if(waitPhotos != null && waitPhotos.size() != 0){
                                                for (int m = 0; m < waitPhotos.size(); m++) {
                                                    if(waitPhotos.get(m).equals(uploadedSellerPhoto.getLocalPath())){
                                                        waitPhotos.set(m, uploadedSellerPhoto.getPhotoUrl());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    List<SellersRecord> uploadSellersRecords = uploadedTaskStepResult.getRecordList();
                    List<SellersRecord> waitSellersRecords = waitTaskStepResult.getRecordList();
                    if(waitSellersRecords != null && waitSellersRecords.size() != 0){
                        for (int k = 0; k < waitSellersRecords.size(); k++) {
                            SellersRecord waitSellerRecord = waitSellersRecords.get(k);
                            for (int l = 0; l < uploadSellersRecords.size(); l++) {
                                SellersRecord uploadedSellerRecord = uploadSellersRecords.get(l);
                                if(StringUtil.isNotBlank(waitSellerRecord.getLocalPath()) &&
                                        StringUtil.isNotBlank(uploadedSellerRecord.getLocalPath())) {
                                    if (waitSellerRecord.getLocalPath().equals(uploadedSellerRecord.getLocalPath())) {
                                        if (uploadedSellerRecord.getRecordUrl().startsWith("http")) {
                                            waitSellerRecord.setRecordUrl(uploadedSellerRecord.getRecordUrl());
                                            List<String> waitRecordss = waitTaskStepResult.getRecords();
                                            if(waitRecordss != null && waitRecordss.size() != 0){
                                                for (int m = 0; m < waitRecordss.size(); m++) {
                                                    if(waitRecordss.get(m).equals(uploadedSellerRecord.getLocalPath())){
                                                        waitRecordss.set(m, uploadedSellerRecord.getRecordUrl());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    List<SellersVideo> uploadSellersVideos = uploadedTaskStepResult.getVideoList();
                    List<SellersVideo> waitSellersVideos = waitTaskStepResult.getVideoList();
                    if(waitSellersVideos != null && waitSellersVideos.size() != 0){
                        for (int k = 0; k < waitSellersVideos.size(); k++) {
                            SellersVideo waitSellerVideo = waitSellersVideos.get(k);
                            for (int l = 0; l < uploadSellersVideos.size(); l++) {
                                SellersVideo uploadedSellerVideo = uploadSellersVideos.get(l);
                                if(StringUtil.isNotBlank(waitSellerVideo.getThumbLocalPath()) &&
                                        StringUtil.isNotBlank(uploadedSellerVideo.getThumbLocalPath())) {
                                    if (waitSellerVideo.getThumbLocalPath().equals(uploadedSellerVideo.getThumbLocalPath())) {
                                        if (uploadedSellerVideo.getThumbUrl().startsWith("http")) {
                                            waitSellerVideo.setThumbUrl(uploadedSellerVideo.getThumbUrl());
                                            waitSellerVideo.setVideoUrl(uploadedSellerVideo.getVideoUrl());
                                            List<ResultVideo> waitVideos = waitTaskStepResult.getVideos();
                                            if(waitVideos != null && waitVideos.size() != 0){
                                                for (int m = 0; m < waitVideos.size(); m++) {
                                                    ResultVideo waitResultVideo = waitVideos.get(m);
                                                    if(waitResultVideo.getThumbUrl().equals(uploadedSellerVideo.getThumbLocalPath())){
                                                        waitResultVideo.setThumbUrl(uploadedSellerVideo.getThumbUrl());
                                                        waitResultVideo.setVideoUrl(uploadedSellerVideo.getVideoUrl());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        TempUploadTask waitTempUploadTask = new TempUploadTask(stepIndex,gson.toJson(waitResultList));
        return waitTempUploadTask;
    }

    private BroadcastReceiver uploadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int uploadFinishCount = intent.getIntExtra(UploadTaskStepResultTask.UPLOAD_FINISH_COUNT_KEY, 0);
            int uploadTotalCount = intent.getIntExtra(UploadTaskStepResultTask.UPLOAD_TOTAL_COUNT_KEY, 0);
            Intent multiLinesStatusIntent = new Intent(ACTION_MULTI_UPLOAD_LINES_STATUS);
            if(uploadingTask != null){
                multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_INDEX_KEY, uploadingTask.getStepIndex());
                multiLinesStatusIntent.putExtra(MULTI_UPLOAD_IS_UPDATE_KEY, uploadingTask.isUpdate);
            }
            multiLinesStatusIntent.putExtra(MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY, uploadFinishCount * 100 / uploadTotalCount);
            multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOADED_STEPS_KEY, uploadedStepIndexList);
            multiLinesStatusIntent.putStringArrayListExtra(MULTI_UPLOAD_CURRENT_UPLOAD_WAIT_STEPS_KEY, waitStepIndexList);
            sendBroadcast(multiLinesStatusIntent);
        }
    };
}
