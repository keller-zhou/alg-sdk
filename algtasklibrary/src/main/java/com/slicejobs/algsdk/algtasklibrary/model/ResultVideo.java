package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by keller.zhou on 16/3/16.
 * 上传的食品
 */
public class ResultVideo implements Serializable {

    private String videoUrl;

    private String thumbUrl;

    private String stepId;

    private boolean isCache;

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

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public boolean isCache() {
        return isCache;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }
}
