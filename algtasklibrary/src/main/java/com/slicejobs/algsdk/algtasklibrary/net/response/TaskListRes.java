package com.slicejobs.algsdk.algtasklibrary.net.response;

import com.slicejobs.algsdk.algtasklibrary.model.Task;

import java.util.List;

/**
 * Created by nlmartian on 7/22/15.
 */
public class TaskListRes {
    public long total;
    public long curpage;
    public int pagesize;
    public int start;
    public List<Task> list;
}
