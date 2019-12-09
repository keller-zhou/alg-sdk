package com.slicejobs.algsdk.algtasklibrary.model;


import java.io.Serializable;

public class JsAlertMsg implements Serializable {

    public String getCancelTitle() {
        return cancelTitle;
    }

    public void setCancelTitle(String cancelTitle) {
        this.cancelTitle = cancelTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOkTitle() {
        return okTitle;
    }

    public void setOkTitle(String okTitle) {
        this.okTitle = okTitle;
    }

    public JsAlertMsg(String okTitle, String message, String cancelTitle) {
        this.okTitle = okTitle;
        this.message = message;
        this.cancelTitle = cancelTitle;
    }

    private String cancelTitle;
    private String message;
    private String okTitle;


    public JsAlertMsg() {

    }

}
