package com.slicejobs.algsdk.algtasklibrary.model;

public class SellersRecord {

    private String localPath;
    private String recordUrl;
    private String stepId;
    private boolean isReplenish;
    private boolean isReview;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
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
}
