package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import android.util.Log;

import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.net.response.TaskListRes;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ITaskView;

import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by nlmartian on 7/27/15.
 */
public class TaskPresenter extends BasePresenter {

    public enum OrderBy {

        SALARY("salary"), ENDTIME("endtime"), DISTANCE("distance"), POINTS("points");

        private String value;

        private OrderBy(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static OrderBy getEnum(String value) {
            for (OrderBy v : values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return SALARY;
        }
    }

    public static final String TAG = "TaskPresenter";

    private ITaskView view;

    public TaskPresenter(ITaskView view) {
        this.view = view;
    }



    public void getMyTask(boolean today, int start, String status) {
        String timestamp = DateUtil.getCurrentTime();
        //view.showProgressDialog();
        User user = BizLogic.getCurrentUser();
        String date = DateUtil.getRequestDateString(new Date());
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder sigBuilder = new SignUtil.SignBuilder()
                .put("userid", user.userid)
                .put("start", start + "")
                .put("appId", appId)
                .put("pagesize", "20");
        if (today) {//获取今日任务
            sigBuilder.put("date", date);
        } else if (status.length() > 0) {//通过status获取任务
            sigBuilder.put("status", status);
        } else {
            view.toast("获取任务出错");
            return;
        }
        sigBuilder.put("timestamp", timestamp);
        String sig = sigBuilder.build();
        Observable<Response<TaskListRes>> req = null;
        if (today) {
            req = restClient.provideApi().getMyTodayTasks(user.userid, date, start, timestamp, appId,sig);
        }  else {
            req = restClient.provideApi().getMyTasks(user.userid, start, status, timestamp,appId, sig);
        }
        req.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    //view.dismissProgressDialog();
                    if (res.ret == 0) {
                        if (res.detail != null && res.detail != null && res.detail.list != null) {
                            view.getMyTaskList(res.detail.list, res.detail.start);
                        }
                    } else {
                        view.toast(res.msg);
                        view.serverExecption("getMyTask",null,start,res.msg,false,"","","");
                        if (res.ret == 1 || res.ret == 6) {
                            view.resetAccountDialog();
                        }
                    }
                }, e -> {
                    //view.dismissProgressDialog();
                    //view.toast(SliceApp.CONTEXT.getString(R.string.get_task_err));
                    view.getTaskError();
                    view.serverExecption("getMyTask",null,start,e.getMessage(),false,"","","");
                    Log.e(TAG, "getMyTask", e);
                });
    }

    public void getNearbyTask(int start, String latitude, String longitude, String disctance, OrderBy orderBy) {
        String timestamp = DateUtil.getCurrentTime();
        //view.showProgressDialog();
        User user = BizLogic.getCurrentUser();
        String date = DateUtil.getRequestDateString(new Date());
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder sigBuilder = new SignUtil.SignBuilder()
                .put("userid", user.userid)
                .put("start", start + "")
                .put("orderby", orderBy.toString())
                .put("distance", disctance)
                .put("pagesize", "100")
                .put("cellphonetype", "10")
                .put("lat", latitude)
                .put("lon", longitude)
                .put("appId", appId)
                .put("timestamp", timestamp);

        String sig = sigBuilder.build();
        Observable<Response<TaskListRes>> req = null;
        req = restClient.provideApi().getMyNearbyTasks(user.userid, start, orderBy.toString(), disctance, latitude, longitude, timestamp,appId, sig);
        req.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    //view.dismissProgressDialog();
                    if (res.ret == 0) {
                        if (res.detail != null && res.detail != null && res.detail.list != null) {
                            view.showTaskList(res.detail.list, res.detail.start);
                        }
                    } else {
                        view.toast(res.msg);
                        view.getTaskError();
                        view.serverExecption("getNearbyTask",orderBy,start,res.msg,false,latitude,longitude,disctance);
                        if (res.ret == 1 || res.ret == 6) {
                            view.resetAccountDialog();
                        }
                    }
                }, e -> {
                    //view.dismissProgressDialog();
                    view.getTaskError();
                    view.serverExecption("getNearbyTask",orderBy,start,e.getMessage(),false,latitude,longitude,disctance);
                    //view.toast(SliceApp.CONTEXT.getString(R.string.get_task_err));
                    Log.e(TAG, "getMyNearbyTask", e);
                });
    }

    public void getNearbyTask(String latitude, String longitude, String lonUser, String latUser, String disctance) {
        String timestamp = DateUtil.getCurrentTime();
        //view.showProgressDialog();
        User user = BizLogic.getCurrentUser();
        String date = DateUtil.getRequestDateString(new Date());
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder sigBuilder = new SignUtil.SignBuilder()
                .put("userid", user.userid)
                .put("distance", disctance)
                .put("pagesize", "100")
                .put("cellphonetype", "10")
                .put("lat", latitude)
                .put("lon", longitude)
                .put("lon_user", lonUser)
                .put("lat_user", latUser)
                .put("appId", appId)
                .put("timestamp", timestamp);

        String sig = sigBuilder.build();
        Observable<Response<TaskListRes>> req = null;
        req = restClient.provideApi().getMyNearbyTasks(user.userid,disctance,"100","10", latitude, longitude, lonUser,latUser,timestamp, appId,sig);
        req.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    //view.dismissProgressDialog();
                    if (res.ret == 0) {
                        if (res.detail != null && res.detail != null && res.detail.list != null) {
                            view.showTaskList(res.detail.list, res.detail.start);
                        }
                    } else {
                        view.toast(res.msg);
                        view.getTaskError();
                        //view.serverExecption("getNearbyTask",TaskPresenter.OrderBy.DISTANCE,0,res.msg,false,latitude,longitude,disctance);
                        if (res.ret == 1 || res.ret == 6) {
                            view.resetAccountDialog();
                        }
                    }
                }, e -> {
                    //view.dismissProgressDialog();
                    view.getTaskError();
                    //view.serverExecption("getNearbyTask",TaskPresenter.OrderBy.DISTANCE,0,e.getMessage(),false,latitude,longitude,disctance);
                    //view.toast(SliceApp.CONTEXT.getString(R.string.get_task_err));
                    Log.e(TAG, "getMyNearbyTask", e);
                });
    }

    public void getNearbyTaskByKeyword(String latitude, String longitude, String disctance, String keyword) {
        String timestamp = DateUtil.getCurrentTime();
        //view.showProgressDialog();
        User user = BizLogic.getCurrentUser();
        String date = DateUtil.getRequestDateString(new Date());
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder sigBuilder = new SignUtil.SignBuilder()
                .put("userid", user.userid)
                .put("distance", disctance)
                .put("pagesize", "100")
                .put("cellphonetype", "10")
                .put("start", "0")
                .put("orderby", "distance")
                .put("lat", latitude)
                .put("lon", longitude)
                .put("keyword", keyword)
                .put("appId", appId)
                .put("timestamp", timestamp);

        String sig = sigBuilder.build();
        Observable<Response<TaskListRes>> req = null;
        req = restClient.provideApi().getMyNearbyTasksByKeyword(user.userid,disctance,"100","10", "0","distance", latitude, longitude,keyword,timestamp, appId,sig);
        req.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    //view.dismissProgressDialog();
                    if (res.ret == 0) {
                        if (res.detail != null && res.detail != null && res.detail.list != null) {
                            view.showKeywordTaskList(res.detail.list);
                        }
                    } else {
                        view.toast(res.msg);
                        view.getTaskError();
                        if (res.ret == 1 || res.ret == 6) {
                            view.resetAccountDialog();
                        }
                    }
                }, e -> {
                    //view.dismissProgressDialog();
                    view.getTaskError();
                });
    }

    public void getNearbyTwentyTask(int start, String latitude, String longitude, String disctance, OrderBy orderBy) {
        String timestamp = DateUtil.getCurrentTime();
        //view.showProgressDialog();
        User user = BizLogic.getCurrentUser();
        String date = DateUtil.getRequestDateString(new Date());
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder sigBuilder = new SignUtil.SignBuilder()
                .put("userid", user.userid)
                .put("start", start + "")
                .put("orderby", orderBy.toString())
                .put("distance", disctance)
                .put("pagesize", "20")
                .put("cellphonetype", "10")
                .put("lat", latitude)
                .put("lon", longitude)
                .put("appId", appId)
                .put("timestamp", timestamp);

        String sig = sigBuilder.build();
        Observable<Response<TaskListRes>> req = null;
        req = restClient.provideApi().getMyNearbyTwentyTasks(user.userid, start, orderBy.toString(), disctance, latitude, longitude, timestamp, appId,sig);
        req.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    //view.dismissProgressDialog();
                    if (res.ret == 0) {
                        if (res.detail != null && res.detail != null && res.detail.list != null) {
                            view.showTaskList(res.detail.list, res.detail.start);
                        }
                    } else {
                        view.toast(res.msg);
                        view.getTaskError();
                        view.serverExecption("getNearbyTwentyTask",orderBy,start,res.msg,false,latitude,longitude,disctance);
                        if (res.ret == 1 || res.ret == 6) {
                            view.resetAccountDialog();
                        }
                    }
                }, e -> {
                    //view.dismissProgressDialog();
                    view.getTaskError();
                    view.serverExecption("getNearbyTwentyTask",orderBy,start,e.getMessage(),false,latitude,longitude,disctance);
                    //view.toast(SliceApp.CONTEXT.getString(R.string.get_task_err));
                    Log.e(TAG, "getMyNearbyTask", e);
                });
    }

}
