package com.slicejobs.algsdk.algtasklibrary.model;

/**
 * Created by keller.zhou on 16/3/23.
 */
public class Upload {

    private String path;//上传本地路径

    private String url;//成功保存的url

    private int progress;//进度

    private String title;

    private String type;//凭证类型

    private String stepId;

    private boolean isCache;

    private boolean isFirstCommit = true;

    private int stepIndex;

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isFirstCommit() {
        return isFirstCommit;
    }

    public void setFirstCommit(boolean firstCommit) {
        isFirstCommit = firstCommit;
    }
}
