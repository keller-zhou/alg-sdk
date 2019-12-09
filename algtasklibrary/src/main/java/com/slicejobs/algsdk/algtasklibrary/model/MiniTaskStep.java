package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by nlmartian on 11/16/15.
 */
public class MiniTaskStep implements Serializable {

    public static final String EVIDENCETYPE_VIDEO ="video";
    public static final String EVIDENCETYPE_PHPTO = "photo";
    public static final String EVIDENCETYPE_RECORD = "record";

    private String stepId;
    private String evidenceType = EVIDENCETYPE_PHPTO; //photo   video    record

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }


    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

}
