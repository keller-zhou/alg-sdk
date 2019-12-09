package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nlmartian on 11/16/15.
 */
public class Option implements Serializable {
    private String optionId;
    private String title;
    private boolean needPhoto;
    private List<String> examplePhotos = new ArrayList<>();
    private String exampleText;
    private boolean forceCamera;
    private String evidenceType;
    private int takePhotoAuxiliaryLine;

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNeedPhoto() {
        return needPhoto;
    }

    public void setNeedPhoto(boolean needPhoto) {
        this.needPhoto = needPhoto;
    }

    public List<String> getExamplePhotos() {
        return examplePhotos;
    }

    public void setExamplePhotos(List<String> examplePhotos) {
        this.examplePhotos = examplePhotos;
    }

    public String getExampleText() {
        return exampleText;
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    public boolean isForceCamera() {
        return forceCamera;
    }

    public void setForceCamera(boolean forceCamera) {
        this.forceCamera = forceCamera;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public int getTakePhotoAuxiliaryLine() {
        return takePhotoAuxiliaryLine;
    }

    public void setTakePhotoAuxiliaryLine(int takePhotoAuxiliaryLine) {
        this.takePhotoAuxiliaryLine = takePhotoAuxiliaryLine;
    }
}
