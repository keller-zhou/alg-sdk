package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;
import java.util.List;

/**
 * 多选项的option
 * Created by xiaoying on 16/3/16.
 */
public class MOption implements Serializable {
    private String id;
    private String title;
    private List<String> exclusiveOption;//互斥

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getExclusiveOption() {
        return exclusiveOption;
    }

    public void setExclusiveOption(List<String> exclusiveOption) {
        this.exclusiveOption = exclusiveOption;
    }
}
