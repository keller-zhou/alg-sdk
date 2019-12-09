package com.slicejobs.algsdk.algtasklibrary.model;

public class JsFileConfig {
    private String fileName;
    private String fileMD5;
    private String downloadUrl;
    public boolean isUseOnline;
    private boolean showLoading = true;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isShowLoading() {
        return showLoading;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
    }

    public JsFileConfig() {
    }

    public JsFileConfig(String fileName, String fileMD5, String downloadUrl, boolean isUseOnline, boolean showLoading) {
        this.fileName = fileName;
        this.fileMD5 = fileMD5;
        this.downloadUrl = downloadUrl;
        this.isUseOnline = isUseOnline;
        this.showLoading = showLoading;
    }
}
