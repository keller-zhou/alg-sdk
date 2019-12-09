package com.slicejobs.algsdk.algtasklibrary.model;

public class TaskStepParams {

    private String taskStepMode;
    private String currentEndTime;
    private String delaymins;
    private String errormessage;
    private Task task;
    private boolean selectAlgCamera;
    private SignInResult signInResult;

    public String getTaskStepMode() {
        return taskStepMode;
    }

    public void setTaskStepMode(String taskStepMode) {
        this.taskStepMode = taskStepMode;
    }

    public String getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(String currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public String getDelaymins() {
        return delaymins;
    }

    public void setDelaymins(String delaymins) {
        this.delaymins = delaymins;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isSelectAlgCamera() {
        return selectAlgCamera;
    }

    public void setSelectAlgCamera(boolean selectAlgCamera) {
        this.selectAlgCamera = selectAlgCamera;
    }
}
