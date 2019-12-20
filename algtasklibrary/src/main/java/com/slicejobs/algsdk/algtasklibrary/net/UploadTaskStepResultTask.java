package com.slicejobs.algsdk.algtasklibrary.net;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTask;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.model.SellersPhoto;
import com.slicejobs.algsdk.algtasklibrary.model.SellersRecord;
import com.slicejobs.algsdk.algtasklibrary.model.SellersVideo;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepResult;
import com.slicejobs.algsdk.algtasklibrary.model.Upload;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.oss.OnUploadListener;
import com.slicejobs.algsdk.algtasklibrary.net.oss.OssUploader;
import com.slicejobs.algsdk.algtasklibrary.service.TaskResultUploadService;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadTaskStepResultTask extends AsyncTask {

    public static final String ACTION_UPLOAD_PROGRESS = "com.slicejobs.ailinggong.ACTION_UPLOAD_PROGRESS";
    public static final String UPLOAD_FINISH_COUNT_KEY = "com.slicejobs.ailinggong.upload_finish_count_key";
    public static final String UPLOAD_TOTAL_COUNT_KEY = "com.slicejobs.ailinggong.upload_total_count_key";

    private Context context;
    private MiniTask task;
    private String resultJson;
    private String evidenceDir;
    private UploadTaskStepResultListener uploadTaskStepResultListener;
    private Gson gson;
    private Map<String, TaskStepResult> taskStepResultMap;
    private List<TaskStepResult> resultList;
    private List<MiniTaskStep> taskSteps;
    private int id= 1;
    private boolean ifFirstCommit = true;//任务是否第一次提交
    private ArrayList<Upload> currentTasks;
    private User user = BizLogic.getCurrentUser();;
    private int uploadFinishCount = 0;
    private boolean isCacel;

    public void setCacel(boolean cacel) {
        isCacel = cacel;
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0x10001) {
                if (uploadTaskStepResultListener != null) {
                    uploadTaskStepResultListener.onUploadError();
                }
            } else if (msg.what == 0x10003) {
                if (uploadTaskStepResultListener != null) {
                    uploadTaskStepResultListener.onUploadFinished(resultJson);
                }
            }else {

            }

            super.handleMessage(msg);
        }
    };

    public void setUploadTaskStepResultListener(UploadTaskStepResultListener uploadTaskStepResultListener) {
        this.uploadTaskStepResultListener = uploadTaskStepResultListener;
    }

    public UploadTaskStepResultTask(Context context, MiniTask task, String resultJson, String evidenceDir){
        this.context = context;
        this.resultJson = resultJson;
        this.evidenceDir = evidenceDir;
        this.task = task;
        this.ifFirstCommit = task.isIfFirstCommit();
        this.taskSteps = this.task.getTaskSteps();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Object[] objects) {
        currentTasks = (ArrayList<Upload>) createUploadTask(resultJson);
        uploadFinishCount = 0;
        if (currentTasks.size() == 0) {//没有上传任务
            String orderResultJsonPath = evidenceDir + File.separator + "orderResultJson.json";
            try {
                FileUtil.writeFileSdcardFile(orderResultJsonPath, resultJson);
                startUploadFile(orderResultJsonPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            for (Upload upload:currentTasks) {
                if(!isCacel) {
                    if (upload.getType().equals(TaskStep.EVIDENCETYPE_PHPTO)) {
                        startUpload(upload.getStepIndex(),upload.getStepId(), upload.isFirstCommit(), upload.isCache(), upload.getPath(), upload);
                    } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                        startUploadVideo(upload.getStepIndex(),upload.getStepId(), upload.isFirstCommit(), upload.isCache(), upload.getPath(), upload);
                    } else if (upload.getType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                        startUploadRecord(upload.getStepIndex(),upload.getStepId(), upload.isFirstCommit(), upload.isCache(), upload.getPath(), upload);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建上传任务
     * @return
     */
    public List<Upload> createUploadTask(String resultJson) {
        taskStepResultMap = new HashMap<>();
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

        List<Upload> list = new ArrayList<>();
        if (taskSteps != null) {
            for (int i = 0;i<taskSteps.size();i++) {
                MiniTaskStep taskStep = taskSteps.get(i);
                if(taskStep != null) {
                    TaskStepResult taskStepResult = taskStepResultMap.get(taskStep.getStepId());
                    if (taskStepResult == null) {
                        continue;
                    }

                    if (taskStep.getEvidenceType().equals(TaskStep.EVIDENCETYPE_VIDEO)) {
                        List<SellersVideo> videoList = taskStepResult.getVideoList();
                        List<ResultVideo> videos = taskStepResult.getVideos();
                        List<ResultVideo> tempVideos = new ArrayList<>();
                        for (SellersVideo resultVideo : videoList) {
                            if (!resultVideo.getThumbUrl().contains("http://")) {
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_PHPTO);
                                upload.setPath(resultVideo.getThumbUrl());
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setCache(resultVideo.isReplenish());
                                upload.setFirstCommit(ifFirstCommit);
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
                                upload.setCache(resultVideo.isReplenish());
                                upload.setFirstCommit(ifFirstCommit);
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }

                            }
                            for (ResultVideo video : videos) {
                                if (video.getVideoUrl().equals(resultVideo.getVideoUrl())) {
                                    tempVideos.add(video);
                                }
                            }
                        }

                        videos.removeAll(tempVideos);
                        if (videos.size() != 0) {
                            for (ResultVideo resultVideo : videos) {
                                if (!resultVideo.getThumbUrl().contains("http://")) {
                                    Upload upload = new Upload();
                                    upload.setStepIndex(i + 1);
                                    upload.setType(TaskStep.EVIDENCETYPE_PHPTO);
                                    upload.setPath(resultVideo.getThumbUrl());
                                    upload.setProgress(0);
                                    upload.setTitle(id + "");
                                    upload.setFirstCommit(ifFirstCommit);
                                    id++;
                                    upload.setStepId(taskStep.getStepId());
                                    upload.setFirstCommit(ifFirstCommit);
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
                                    upload.setFirstCommit(ifFirstCommit);
                                    id++;
                                    upload.setStepId(taskStep.getStepId());
                                    upload.setFirstCommit(ifFirstCommit);
                                    File file = new File(upload.getPath());
                                    if (file.exists()) {
                                        list.add(upload);
                                    }

                                }
                            }
                        }

                    } else if (taskStep.getEvidenceType().equals(TaskStep.EVIDENCETYPE_RECORD)) {
                        List<String> records = taskStepResult.getRecords();
                        List<String> tempRecords = new ArrayList<>();
                        for (SellersRecord sellersEvidence : taskStepResult.getRecordList()) {
                            String str = sellersEvidence.getRecordUrl();
                            if (!str.contains("http://")) {
                                Upload upload = new Upload();
                                upload.setStepIndex(i + 1);
                                upload.setType(TaskStep.EVIDENCETYPE_RECORD);
                                upload.setPath(str);
                                upload.setProgress(0);
                                upload.setTitle(id + "");
                                id++;
                                upload.setStepId(taskStep.getStepId());
                                upload.setCache(sellersEvidence.isReplenish());
                                upload.setFirstCommit(ifFirstCommit);
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }
                            }
                            for (String recordPath : records) {
                                if (recordPath.equals(str)) {
                                    tempRecords.add(recordPath);
                                }
                            }
                        }
                        records.removeAll(tempRecords);
                        if (records.size() != 0) {
                            for (String str : records) {
                                if (!str.contains("http://")) {
                                    Upload upload = new Upload();
                                    upload.setStepIndex(i + 1);
                                    upload.setType(TaskStep.EVIDENCETYPE_RECORD);
                                    upload.setPath(str);
                                    upload.setProgress(0);
                                    upload.setTitle(id + "");
                                    upload.setFirstCommit(ifFirstCommit);
                                    id++;
                                    upload.setStepId(taskStep.getStepId());
                                    upload.setFirstCommit(ifFirstCommit);
                                    File file = new File(upload.getPath());
                                    if (file.exists()) {
                                        list.add(upload);
                                    }
                                }
                            }
                        }

                    } else {
                        List<String> photos = taskStepResult.getPhotos();
                        List<String> tempPhotos = new ArrayList<>();
                        for (SellersPhoto sellersEvidence : taskStepResult.getPhotoList()) {
                            String str = sellersEvidence.getPhotoUrl();
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
                                upload.setCache(sellersEvidence.isReplenish());
                                upload.setFirstCommit(ifFirstCommit);
                                File file = new File(upload.getPath());
                                if (file.exists()) {
                                    list.add(upload);
                                }
                            }
                            for (String photoPath : photos) {
                                if (photoPath.equals(str)) {
                                    tempPhotos.add(photoPath);
                                }
                            }
                        }
                        photos.removeAll(tempPhotos);

                        if (photos.size() != 0) {
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
                                    upload.setFirstCommit(ifFirstCommit);
                                    id++;
                                    upload.setStepId(taskStep.getStepId());
                                    upload.setFirstCommit(ifFirstCommit);
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

        }

        return list;
    }

    private void startUpload(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload) {
        new OssUploader(SliceApp.CONTEXT).uploadTaskCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getOrderid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                for (Upload upload1 : currentTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                //upload.setUrl(url);
                checkUploadFinlish();
            }

            @Override
            public void onUploadFail(String msg) {
                if (msg.contains("403")) {
                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
                }
                handler.sendEmptyMessage(0x10001);
            }

            @Override
            public void onUploadProgress(int percent) {

            }
        });
    }

    private void startUploadVideo(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload) {
        new OssUploader(SliceApp.CONTEXT).uploadTaskVideoCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getTaskid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                for (Upload upload1 : currentTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                //upload.setUrl(url);
                checkUploadFinlish();
            }


            @Override
            public void onUploadFail(String msg) {
                if (msg.contains("403")) {
                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
                }
                handler.sendEmptyMessage(0x10001);
            }

            @Override
            public void onUploadProgress(int percent) {

            }
        });
    }

    private void startUploadRecord(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, final String filePath, Upload upload) {
        new OssUploader(SliceApp.CONTEXT).uploadTaskRecordCert(stepIndex,stepId,isFirstCommit,isCache,user.userid, task.getTaskid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                for (Upload upload1 : currentTasks) {
                    if (upload1 != null && upload1.getPath().equals(upload.getPath())) {
                        upload1.setUrl(url);
                    }
                }
                checkUploadFinlish();
            }

            @Override
            public void onUploadFail(String msg) {
                if (msg.contains("403")) {
                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
                }
                handler.sendEmptyMessage(0x10001);
            }

            @Override
            public void onUploadProgress(int percent) {

            }
        });
    }

    private void startUploadFile(String filePath) {
        new OssUploader(SliceApp.CONTEXT).uploadTaskJsonCert(task.getOrderid(), filePath, new OnUploadListener() {
            @Override
            public void onUploadSuccess(String url) {
                Log.d("-----", "------" + url);
                Intent multiLinesStatusIntent = new Intent(TaskResultUploadService.ACTION_MULTI_UPLOAD_LINES_STATUS);
                multiLinesStatusIntent.putExtra(TaskResultUploadService.MULTI_UPLOAD_CURRENT_LINE_PROGRESS_KEY,99);
                SliceApp.CONTEXT.sendBroadcast(multiLinesStatusIntent);
                handler.sendEmptyMessage(0x10003);
            }

            @Override
            public void onUploadFail(String msg) {
                if (msg.contains("403")) {
                    PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除oss token
                }
                handler.sendEmptyMessage(0x10001);
            }

            @Override
            public void onUploadProgress(int percent) {

            }
        });
    }

    /**
     * 检测是否上传完成
     * @return
     */
    public void checkUploadFinlish() {
        if(!isCacel) {
            uploadFinishCount += 1;
            Intent intentUploadProgress = new Intent(ACTION_UPLOAD_PROGRESS);
            intentUploadProgress.putExtra(UPLOAD_FINISH_COUNT_KEY, uploadFinishCount);
            intentUploadProgress.putExtra(UPLOAD_TOTAL_COUNT_KEY, currentTasks.size());
            context.sendBroadcast(intentUploadProgress);
            boolean flag = true;
            for (Upload upload1 : currentTasks) {
                if (StringUtil.isBlank(upload1.getUrl())) {
                    flag = false;
                }
            }

            if (flag) {
                //打包
                for (Upload upload : currentTasks) {
                    if (resultJson.contains(upload.getPath())) {
                        resultJson = getUploadedResultJson();
                    }
                }
                String orderResultJsonPath = evidenceDir + File.separator + "orderResultJson.json";
                try {
                    FileUtil.writeFileSdcardFile(orderResultJsonPath, resultJson);
                    startUploadFile(orderResultJsonPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface UploadTaskStepResultListener{
        void onUploadFinished(String result);
        void onUploadError();
    }

    //打包替换本地地址为网络地址，但是localPath thumbLocalPath不换
    private String getUploadedResultJson(){
        List<TaskStepResult> taskStepResultList = gson.fromJson(
                resultJson, new TypeToken<List<TaskStepResult>>() {
                }.getType());
        for (Upload upload : currentTasks) {
            for(TaskStepResult taskStepResult:taskStepResultList){
                List<String> photos = taskStepResult.getPhotos();
                for (int i = 0; i < photos.size(); i++) {
                    if(photos.get(i).equals(upload.getPath())){
                        photos.set(i, upload.getUrl());
                    }
                }
                List<String> records = taskStepResult.getRecords();
                for (int i = 0; i < records.size(); i++) {
                    if(records.get(i).equals(upload.getPath())){
                        records.set(i, upload.getUrl());
                    }
                }
                List<ResultVideo> videos = taskStepResult.getVideos();
                for (int i = 0; i < videos.size(); i++) {
                    ResultVideo resultVideo = videos.get(i);
                    if(resultVideo.getThumbUrl().equals(upload.getPath())){
                        resultVideo.setThumbUrl(upload.getUrl());
                    }
                    if(resultVideo.getVideoUrl().equals(upload.getPath())){
                        resultVideo.setVideoUrl(upload.getUrl());
                    }
                }
                List<SellersPhoto> photoList = taskStepResult.getPhotoList();
                for (int i = 0; i < photoList.size(); i++) {
                    SellersPhoto sellersPhoto = photoList.get(i);
                    if(sellersPhoto.getPhotoUrl().equals(upload.getPath())){
                        sellersPhoto.setPhotoUrl(upload.getUrl());
                    }
                }
                List<SellersRecord> recordList = taskStepResult.getRecordList();
                for (int i = 0; i < recordList.size(); i++) {
                    SellersRecord sellersRecord = recordList.get(i);
                    if(sellersRecord.getRecordUrl().equals(upload.getPath())){
                        sellersRecord.setRecordUrl(upload.getUrl());
                    }
                }
                List<SellersVideo> videoList = taskStepResult.getVideoList();
                for (int i = 0; i < videoList.size(); i++) {
                    SellersVideo sellersVideo = videoList.get(i);
                    if(sellersVideo.getThumbUrl().equals(upload.getPath())){
                        sellersVideo.setThumbUrl(upload.getUrl());
                    }
                    if(sellersVideo.getVideoUrl().equals(upload.getPath())){
                        sellersVideo.setVideoUrl(upload.getUrl());
                    }
                }
            }
        }
        return gson.toJson(taskStepResultList);
    }
}
