package com.slicejobs.algsdk.algtasklibrary.model;

public class SellersPhoto {

    private String localPath;
    private String photoUrl;
    private String stepId;
    private boolean isReplenish;
    private boolean isReview;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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
