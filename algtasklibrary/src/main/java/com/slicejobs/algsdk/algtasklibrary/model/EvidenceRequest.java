package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by keller.zhou on 17/3/22.
 * 对证据的要求
 */
public class EvidenceRequest implements Serializable {
    public static final String PHOTO_QUALITY_LOW = "1";//照片低质量
    public static final String PHOTO_QUALITY_MIDDLE = "2"; //默认质量
    public static final String PHOTO_QUALITY_UPPER = "3";//高质量
    public static final String PHOTO_QUALITY_HIGHEST = "4";//超高质量

    private String quality = PHOTO_QUALITY_MIDDLE;//照片质量

    private float clarity = 0;//照片清晰度,默认不检查清晰度

    private String location = "0,0";//照片拍摄的位置

    private int videoDuration = 0;//视屏限制时间，单位s

    private int recordDuration = 0;//录音限制时间，单位s

    public String[] getImageRecognitionType() {
        return imageRecognitionType;
    }

    public void setImageRecognitionType(String[] imageRecognitionType) {
        this.imageRecognitionType = imageRecognitionType;
    }

    private String[] imageRecognitionType;//照片筛选 0不开启 1近景 2货架(正面) 3货架(倾斜)

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public float getClarity() {
        return clarity;
    }

    public void setClarity(float clarity) {
        this.clarity = clarity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public int getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(int recordDuration) {
        this.recordDuration = recordDuration;
    }
}
