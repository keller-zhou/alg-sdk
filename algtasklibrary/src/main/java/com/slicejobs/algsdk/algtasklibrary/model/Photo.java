package com.slicejobs.algsdk.algtasklibrary.model;

/**
 * Created by keller.zhou on 16/5/18.
 */
public class Photo {

    private String nativePhotoPath;
    private String nativeThumbnailPath;//缩略图
    private int isClarity;//是否清晰0清晰 1不清晰

    private int imageRecognitionTypeMatch;//照片筛选 0不开启或符合 1不符合

    public int getImageRecognitionTypeMatch() {
        return imageRecognitionTypeMatch;
    }

    public void setImageRecognitionTypeMatch(int imageRecognitionType) {
        this.imageRecognitionTypeMatch = imageRecognitionType;
    }

    public String getNativePhotoPath() {
        return nativePhotoPath;
    }

    public void setNativePhotoPath(String nativePhotoPath) {
        this.nativePhotoPath = nativePhotoPath;
    }

    public String getNativeThumbnailPath() {
        return nativeThumbnailPath;
    }

    public void setNativeThumbnailPath(String nativeThumbnailPath) {
        this.nativeThumbnailPath = nativeThumbnailPath;
    }

    public int getIsClarity() {
        return isClarity;
    }

    public void setIsClarity(int isClarity) {
        this.isClarity = isClarity;
    }
}
