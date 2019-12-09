package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nlmartian on 11/16/15.
 */
public class TaskStepResult implements Serializable {
    private String stepId;
    private String textAnswer;
    private List<String> photos = new ArrayList<>();
    private String selectedOptionId;//单选答案
    private String isPassed = "10";
    private String errormessage;
    private List<String> mOptionIds = new ArrayList<>();//多选答案
    private List<ResultVideo> videos = new ArrayList<>();//视频结果
    private List<String> records = new ArrayList<>();//录音上传
    private Map<String, TabTaskResult> mTextAnswer = new HashMap<>();//多项填空题答案
    private List<String> pictureMultipleOptionIds = new ArrayList<>();
    private List<SellersPhoto> photoList = new ArrayList<>();//促销员备份照片
    private List<SellersVideo> videoList = new ArrayList<>();//促销员备份视频
    private List<SellersRecord> recordList = new ArrayList<>();//促销员备份录音

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(String selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }


    public List<String> getmOptionIds() {
        return mOptionIds;
    }

    public void setmOptionIds(List<String> mOptionIds) {
        this.mOptionIds = mOptionIds;
    }

    public List<ResultVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<ResultVideo> videos) {
        this.videos = videos;
    }

    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }

    public String getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(String isPassed) {
        this.isPassed = isPassed;
    }

    public Map<String, TabTaskResult> getmTextAnswer() {
        return mTextAnswer;
    }

    public void setmTextAnswer(Map<String, TabTaskResult> mTextAnswer) {
        this.mTextAnswer = mTextAnswer;
    }

    public List<String> getPictureMultipleOptionIds() {
        return pictureMultipleOptionIds;
    }

    public void setPictureMultipleOptionIds(List<String> pictureMultipleOptionIds) {
        this.pictureMultipleOptionIds = pictureMultipleOptionIds;
    }

    public List<SellersPhoto> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<SellersPhoto> photoList) {
        this.photoList = photoList;
    }

    public List<SellersVideo> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<SellersVideo> videoList) {
        this.videoList = videoList;
    }

    public List<SellersRecord> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<SellersRecord> recordList) {
        this.recordList = recordList;
    }
}
