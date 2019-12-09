package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by nlmartian on 8/15/15.
 */
public class ShareBean implements Serializable {
    private String shareTitle;
    private String shareContent;
    private String shareImageUrl;
    private String shareUrl;
    private String shareActionId;
    private boolean isShare;
    private String fair_type;

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public String getShareImageUrl() {
        return shareImageUrl;
    }

    public void setShareImageUrl(String shareImageUrl) {
        this.shareImageUrl = shareImageUrl;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getShareActionId() {
        return shareActionId;
    }

    public void setShareActionId(String shareActionId) {
        this.shareActionId = shareActionId;
    }

    public boolean isShare() {
        return isShare;
    }

    public void setIsShare(boolean isShare) {
        this.isShare = isShare;
    }

    public String getFair_type() {
        return fair_type;
    }

    public void setFair_type(String fair_type) {
        this.fair_type = fair_type;
    }

    public ShareBean() {
    }

    public ShareBean(String shareTitle, String shareContent, String shareImageUrl, String shareUrl, String shareActionId, boolean isShare, String fair_type) {
        this.shareTitle = shareTitle;
        this.shareContent = shareContent;
        this.shareImageUrl = shareImageUrl;
        this.shareUrl = shareUrl;
        this.shareActionId = shareActionId;
        this.isShare = isShare;
        this.fair_type = fair_type;
    }
}
