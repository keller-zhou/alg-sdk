package com.slicejobs.algsdk.algtasklibrary.net.oss;

public interface OnUploadListener {
    public void onUploadSuccess(String url);

    public void onUploadFail(String msg);

    public void onUploadProgress(int percent);
}