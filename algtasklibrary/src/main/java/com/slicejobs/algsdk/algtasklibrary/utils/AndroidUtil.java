package com.slicejobs.algsdk.algtasklibrary.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;

import java.util.List;

/**
 * Created by nlmartian on 9/19/15.
 */
public class AndroidUtil {
    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void updateSystemGallery(String path) {
        try {
            //把文件插入到系统图库
            // 最后通知图库更新
            SliceApp.CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + path)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断前台后台
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查密码强度
     * @param curr
     * @return 0不合法 1 弱  2中 3强
     */
    public static int checkPassWordStrength(CharSequence curr) {
        int currLenght = curr.length();
        int result = 1;

        if (currLenght <=9){ result = 2;}

        if (currLenght <= 9 && curr.toString().matches("[0-9]+")) {//小于9位，数字，弱
            result = 1;
        }

        if((currLenght > 9 && currLenght <= 14)){
            result = 3;
        }

        if ((currLenght > 9 && currLenght <= 14) && curr.toString().matches("[0-9]+")) {//中
            result = 2;
        }

        if (currLenght > 14) {
            result = 3;
        }

        if (currLenght < 6) {
            result = 0;
        }

        return result;

    }

    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

}
