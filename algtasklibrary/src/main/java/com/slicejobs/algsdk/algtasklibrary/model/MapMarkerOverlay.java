package com.slicejobs.algsdk.algtasklibrary.model;

/**
 * Created by keller.zhou on 16/6/29.
 * 地图门店覆盖物
 */
public class MapMarkerOverlay {

    public int getMarketType() {
        return marketType;
    }

    public void setMarketType(int marketType) {
        this.marketType = marketType;
    }

    private int marketType;//门店的类型 1 普通的任务门店 2，用户已抢任务所在的门店 3，用户点击任务的门店
    private float sumMoney;//门店总金额
    private int sumPoint;//门店总零豆

    private double lat;

    private double lon;

    private String addRess;//门店地址

    public MapMarkerOverlay() {}


    public MapMarkerOverlay(int marketType, float sumMoney, int sumPoint, double lat, double lon, String addRess) {
        this.marketType = marketType;
        this.sumMoney = sumMoney;
        this.sumPoint = sumPoint;
        this.lat = lat;
        this.lon = lon;
        this.addRess = addRess;
    }

    public float getSumMoney() {
        return sumMoney;
    }

    public void setSumMoney(float sumMoney) {
        this.sumMoney = sumMoney;
    }

    public int getSumPoint() {
        return sumPoint;
    }

    public void setSumPoint(int sumPoint) {
        this.sumPoint = sumPoint;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getAddRess() {
        return addRess;
    }

    public void setAddRess(String addRess) {
        this.addRess = addRess;
    }
}
