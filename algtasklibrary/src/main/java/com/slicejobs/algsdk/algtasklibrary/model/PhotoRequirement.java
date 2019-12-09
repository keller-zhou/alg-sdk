package com.slicejobs.algsdk.algtasklibrary.model;


import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nlmartian on 12/2/15.
 */
public class PhotoRequirement implements Serializable {

    private String photoSource;
    private boolean forceCamera;
    private List<String> examplePhotos; // 拍照实例
    private String exampleText;
    private String evidenceType;//当选题的结果类型
    private boolean allowReusePhoto;
    private String stepId;
    private int takePhotoAuxiliaryLine;

    public PhotoRequirement(Option option) {
        this.forceCamera = option.isForceCamera();
        this.examplePhotos = new ArrayList<>();
        if(option.getExamplePhotos() != null && option.getExamplePhotos().size() != 0) {
            this.examplePhotos.addAll(option.getExamplePhotos());
        }
        this.exampleText = option.getExampleText();
        this.evidenceType = option.getEvidenceType();
        this.takePhotoAuxiliaryLine = option.getTakePhotoAuxiliaryLine();
    }

    public PhotoRequirement(TaskStep step) {
        this.forceCamera = step.isForceCamera();
        this.examplePhotos = new ArrayList<>();
        if(step.getExamplePhotos() != null && step.getExamplePhotos().size() != 0){
            this.examplePhotos.addAll(step.getExamplePhotos());
        }
        this.exampleText = step.getExampleText();
        Log.d("----------------------", "拿到的类型"+step.getEvidenceType());
        this.evidenceType = step.getEvidenceType();
        this.allowReusePhoto = step.isAllowReusePhoto();
        this.stepId = step.getStepId();
        this.takePhotoAuxiliaryLine = step.getTakePhotoAuxiliaryLine();
    }

    public PhotoRequirement(String photoSource, TaskStep step) {
        this.photoSource = photoSource;
        this.forceCamera = step.isForceCamera();
        this.examplePhotos = new ArrayList<>();
        if(step.getExamplePhotos() != null && step.getExamplePhotos().size() != 0){
            this.examplePhotos.addAll(step.getExamplePhotos());
        }
        this.exampleText = step.getExampleText();
        Log.d("----------------------", "拿到的类型"+step.getEvidenceType());
        this.evidenceType = step.getEvidenceType();
        this.allowReusePhoto = step.isAllowReusePhoto();
        this.stepId = step.getStepId();
        this.takePhotoAuxiliaryLine = step.getTakePhotoAuxiliaryLine();
    }

    public String getPhotoSource() {
        return photoSource;
    }

    public void setPhotoSource(String photoSource) {
        this.photoSource = photoSource;
    }

    public boolean isAllowReusePhoto() {
        return allowReusePhoto;
    }

    public void setAllowReusePhoto(boolean allowReusePhoto) {
        this.allowReusePhoto = allowReusePhoto;
    }

    public boolean isForceCamera() {
        return forceCamera;
    }

    public void setForceCamera(boolean forceCamera) {
        this.forceCamera = forceCamera;
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

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public int getTakePhotoAuxiliaryLine() {
        return takePhotoAuxiliaryLine;
    }

    public void setTakePhotoAuxiliaryLine(int takePhotoAuxiliaryLine) {
        this.takePhotoAuxiliaryLine = takePhotoAuxiliaryLine;
    }
}
