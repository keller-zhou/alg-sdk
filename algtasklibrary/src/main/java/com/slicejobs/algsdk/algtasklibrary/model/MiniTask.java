package com.slicejobs.algsdk.algtasklibrary.model;


import java.io.Serializable;
import java.util.List;

/**
 * Created by nlmartian on 7/19/15.
 */
public class MiniTask implements Serializable {
    private String taskid;
    private String orderid;
    private String rfversion;
    private String longitude;
    private String latitude;
    private boolean ifFirstCommit = true;//任务是否第一次提交
    private String market_gatherinfo;
    private List<MiniTaskStep> taskSteps;

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getRfversion() {
        return rfversion;
    }

    public void setRfversion(String rfversion) {
        this.rfversion = rfversion;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public boolean isIfFirstCommit() {
        return ifFirstCommit;
    }

    public void setIfFirstCommit(boolean ifFirstCommit) {
        this.ifFirstCommit = ifFirstCommit;
    }

    public String getMarket_gatherinfo() {
        return market_gatherinfo;
    }

    public void setMarket_gatherinfo(String market_gatherinfo) {
        this.market_gatherinfo = market_gatherinfo;
    }

    public List<MiniTaskStep> getTaskSteps() {
        return taskSteps;
    }

    public void setTaskSteps(List<MiniTaskStep> taskSteps) {
        this.taskSteps = taskSteps;
    }
}
