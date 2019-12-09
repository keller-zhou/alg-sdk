package com.slicejobs.algsdk.algtasklibrary.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;

/**
 * Created by keller.zhou on 16/1/12.
 */
public class VersionUtil {
    public static PackageInfo info = null;


    /**
     * 检测版本对象
     */
    public static PackageInfo slicejobsVersionInfo() {
        if (info != null) {
            return info;

        } else {
            try {
                info = SliceApp.CONTEXT.getPackageManager().getPackageInfo(SliceApp.CONTEXT.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return info;
        }
    }



}
