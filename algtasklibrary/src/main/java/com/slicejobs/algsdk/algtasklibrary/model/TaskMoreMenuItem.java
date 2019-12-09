package com.slicejobs.algsdk.algtasklibrary.model;

public class TaskMoreMenuItem {
    private String tag;
    private int iconId;
    private String text;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TaskMoreMenuItem(String tag, int iconId, String text) {
        this.tag = tag;
        this.iconId = iconId;
        this.text = text;
    }
}
