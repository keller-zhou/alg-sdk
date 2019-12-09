package com.slicejobs.algsdk.algtasklibrary.model;

public class TaskImage {
    private String path;
    private boolean ifSelect;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIfSelect() {
        return ifSelect;
    }

    public void setIfSelect(boolean ifSelect) {
        this.ifSelect = ifSelect;
    }

    public TaskImage() {
    }

    public TaskImage(String path, boolean ifSelect) {
        this.path = path;
        this.ifSelect = ifSelect;
    }
}
