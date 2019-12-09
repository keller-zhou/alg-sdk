package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

public class TaskVideo implements Serializable {

    private String thumbUrl;
    private String videoUrl;


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

    public TaskVideo(String videoUrl, String thumbUrl) {
        this.thumbUrl = thumbUrl;
        this.videoUrl = videoUrl;
    }
}
