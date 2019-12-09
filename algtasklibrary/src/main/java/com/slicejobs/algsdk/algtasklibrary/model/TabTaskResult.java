package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by keller.zhou on 16/9/18.
 */
public class TabTaskResult implements Serializable {

    private String textAnswer;

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }
}
