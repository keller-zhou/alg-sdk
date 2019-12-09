package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;


public class SellersVideo implements Serializable {

    private String videoUrl;
    private String thumbLocalPath;
    private String thumbUrl;

    private String stepId;

    private boolean isReplenish;

    private boolean isReview;
    private String localPath;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getThumbLocalPath() {
        return thumbLocalPath;
    }

    public void setThumbLocalPath(String thumbLocalPath) {
        this.thumbLocalPath = thumbLocalPath;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public boolean isReplenish() {
        return isReplenish;
    }

    public void setReplenish(boolean replenish) {
        isReplenish = replenish;
    }

    public boolean isReview() {
        return isReview;
    }

    public void setReview(boolean review) {
        isReview = review;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
