package com.slicejobs.algsdk.algtasklibrary.app;

import com.slicejobs.algsdk.algtasklibrary.model.Task;

import java.util.Map;

/**
 * Created by nlmartian on 7/11/15.
 */
public class AppEvent {
    public static class Register1Event {
    }

    /**
     * h5触发修改密码
     */
    public static class ModifyPwViewEvent {
        public String eventType;
        public Map<String, Object> params;

        public ModifyPwViewEvent(String eventType, Map<String, Object> ob) {
            this.eventType = eventType;
            this.params = ob;
        }

    }

    /**
     * h5触发关于我们操作
     */
    public static class TaskDetailViewEvent {
        public String eventType;
        public Map<String, Object> params;

        public TaskDetailViewEvent(String eventType, Map<String, Object> ob) {
            this.eventType = eventType;
            this.params = ob;
        }
    }

    /**
     * h5触发分步任务操作
     */
    public static class TaskStepViewEvent {
        public String eventType;
        public Map<String, Object> params;

        public TaskStepViewEvent(String eventType, Map<String, Object> ob) {
            this.eventType = eventType;
            this.params = ob;
        }
    }

    public static class TaskStatusEvent {
        public String taskid;
        public String status;

        public TaskStatusEvent(String taskid, String status) {
            this.taskid = taskid;
            this.status = status;
        }
    }

    public static class RefreshTaskEvent {
        public String status;

        public RefreshTaskEvent(String status) {
            this.status = status;
        }
    }

    /**
     * 微信分享返回监听时间
     */
    public static class WXshareCallback {
        public String result;

        public WXshareCallback(String result) {
            this.result = result;
        }
    }

    public static class ReplaceTaskEvent {
        public String oldTaskId;
        public Task newTask;

        public ReplaceTaskEvent(String oldTaskId, Task newTask) {
            this.oldTaskId = oldTaskId;
            this.newTask = newTask;
        }
    }

    /**
     * 关注门店修改监听
     */
    public static class ModifyMarketViewEvent {
        public Map<String, Object> params;

        public ModifyMarketViewEvent(Map<String, Object> params) {
            this.params = params;
        }
    }
}
