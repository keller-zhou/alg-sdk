package com.slicejobs.algsdk.algtasklibrary.app;


import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;

/**
 * Created by nlmartian on 7/22/15.
 */
public class BizLogic {
    private static double EARTH_RADIUS = 6378.137;

    public static User getCurrentUser() {
        return (User) PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getObject(AppConfig.PREF_USER, User.class);
    }

    public static void updateUser(User user) {
        PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, user);
    }

    public static double coordinate2Distance(String lat1, String lng1, String lat2, String lng2) {
        try {
            double dLat1 = Double.parseDouble(lat1);
            double dLng1 = Double.parseDouble(lng1);
            double dLat2 = Double.parseDouble(lat2);
            double dLng2 = Double.parseDouble(lng2);
            double radLat1 = rad(dLat1);
            double radLat2 = rad(dLat2);
            double a = radLat1 - radLat2;
            double b = rad(dLng1) - rad(dLng2);
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                    Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            //s = Math.round(s * 10000) / 10000;
            return s;
        } catch (Exception e) {
            return 0;
        }
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
