package com.slicejobs.algsdk.algtasklibrary.model;


import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nlmartian on 7/19/15.
 */
public class Task implements Serializable {
    private String title;
    private String taskid;
    private String type;
    private String salary;
    private Date starttime;
    private Date endtime;
    private Date deadline;
    private Date publishtime;
    private Date vietime;
    private String adminname;
    private String longitude;
    private String latitude;
    private String content;
    private String adminid;
    private String memo;
    private String photo;
    private String status;
    private String hirenum;
    private String priority;
    private String marketid;
    private Market marketinfo;
    private String taskmarketareaid;
    private String gpsCheck;
    private String errormessage;
    private String templatejson;
    private String templateresultjson;
    private String distance = "0";
    private String review;
    private String reviewtime;
    private String nearby;
    private String processingtime;
    private Date uservietime;//抢单时间
    private String points;//任务积分
    private String provinceid;
    private String superuserprior;
    private String seriestasktemplatejson;//系列任务
    private String clientversion;//当前做任务时的版本2.0.0
    private String taskurl;//录入任务url
    private String validcheckindistance = "2";//
    private String orderid;
    private String rfversion;
    private String coupon_id;
    private boolean market_need_gatherinfo;
    private String use_servercache = "1";//0 不缓存任务证据 1 缓存任务证据

    public String getMarket_gatherinfo() {
        return market_gatherinfo;
    }

    public void setMarket_gatherinfo(String market_gatherinfo) {
        this.market_gatherinfo = market_gatherinfo;
    }

    private String market_gatherinfo;

    public boolean isMarket_need_gatherinfo() {
        return market_need_gatherinfo;
    }

    public void setMarket_need_gatherinfo(boolean market_need_gatherinfo) {
        this.market_need_gatherinfo = market_need_gatherinfo;
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

    public String getAdminid() {
        return adminid;
    }

    public void setAdminid(String adminid) {
        this.adminid = adminid;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTaskid() {
        if (StringUtil.isNotBlank(rfversion) && rfversion.equals("1") && StringUtil.isNotBlank(orderid)) {
            return orderid;
        } else {
            return taskid;
        }
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getAdminname() {
        return adminname;
    }

    public void setAdminname(String adminname) {
        this.adminname = adminname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHirenum() {
        return hirenum;
    }

    public void setHirenum(String hirenum) {
        this.hirenum = hirenum;
    }

    public Date getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(Date publishtime) {
        this.publishtime = publishtime;
    }

    public String getPriority() {
        if (priority == null || priority.trim().length() == 0) {
            priority = "0";
        }
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMarketid() {
        return marketid;
    }

    public void setMarketid(String marketid) {
        this.marketid = marketid;
    }

    public Market getMarketinfo() {
        return marketinfo;
    }

    public void setMarketinfo(Market marketinfo) {
        this.marketinfo = marketinfo;
    }

    public Date getVietime() {
        return vietime;
    }

    public void setVietime(Date vietime) {
        this.vietime = vietime;
    }

    public String getTaskmarketareaid() {
        return taskmarketareaid;
    }

    public void setTaskmarketareaid(String taskmarketareaid) {
        this.taskmarketareaid = taskmarketareaid;
    }

    public String getGpsCheck() {
        return gpsCheck;
    }

    public void setGpsCheck(String gpsCheck) {
        this.gpsCheck = gpsCheck;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    public String getTemplatejson() {
        return templatejson;
    }

    public void setTemplatejson(String templatejson) {
        this.templatejson = templatejson;
    }

    public String getTemplateresultjson() {
        return templateresultjson;
    }

    public void setTemplateresultjson(String templateresultjson) {
        this.templateresultjson = templateresultjson;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getReviewtime() {
        return reviewtime;
    }

    public void setReviewtime(String reviewtime) {
        this.reviewtime = reviewtime;
    }

    public String getNearby() {
        return nearby;
    }

    public void setNearby(String nearby) {
        this.nearby = nearby;
    }

    public String getProcessingtime() {
        return processingtime;
    }

    public void setProcessingtime(String processingtime) {
        this.processingtime = processingtime;
    }

    public Date getUservietime() {
        return uservietime;
    }

    public void setUservietime(Date uservietime) {
        this.uservietime = uservietime;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getProvinceid() {
        return provinceid;
    }

    public void setProvinceid(String provinceid) {
        this.provinceid = provinceid;
    }


    public String getSuperuserprior() {
        return superuserprior;
    }

    public void setSuperuserprior(String superuserprior) {
        this.superuserprior = superuserprior;
    }

    public String getSeriestasktemplatejson() {
        return seriestasktemplatejson;
    }

    public void setSeriestasktemplatejson(String seriestasktemplatejson) {
        this.seriestasktemplatejson = seriestasktemplatejson;
    }

    public String getClientversion() {
        return clientversion;
    }

    public void setClientversion(String clientversion) {
        this.clientversion = clientversion;
    }

    public String getTaskurl() {
        return taskurl;
    }

    public void setTaskurl(String taskurl) {
        this.taskurl = taskurl;
    }

    public String getValidcheckindistance() {
        return validcheckindistance;
    }

    public void setValidcheckindistance(String validcheckindistance) {
        this.validcheckindistance = validcheckindistance;
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

    public String getRealTaskid() {
        return taskid;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getUse_servercache() {
        return use_servercache;
    }

    public void setUse_servercache(String use_servercache) {
        this.use_servercache = use_servercache;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
