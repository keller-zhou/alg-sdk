package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

public class TempUploadTask implements Serializable {
    private String stepIndex;
    private String resultJson;
    public boolean isUpdate;

    public TempUploadTask(String stepIndex, String resultJson) {
        this.stepIndex = stepIndex;
        this.resultJson = resultJson;
    }

    public String getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(String stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }
}
