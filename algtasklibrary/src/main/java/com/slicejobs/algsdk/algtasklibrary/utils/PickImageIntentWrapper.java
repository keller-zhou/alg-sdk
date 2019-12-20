package com.slicejobs.algsdk.algtasklibrary.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.EvidenceRequest;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.CameraKitActivity;

import java.util.List;

/**
 * Created by martian on 13-11-14.
 */
public class PickImageIntentWrapper {

    public static Intent getPhotoPickIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        return intent;
    }

    public static Intent getVideoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        return intent;
    }

    public static Intent getRecordPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        intent.setType("audio/*");
        return intent;
    }

    /**
     * 系统相机
     * @param uri
     * @return
     */
    public static Intent getTakePickIntent(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            intent = cameraMethod(SliceApp.CONTEXT, uri);//避免安卓使用第三方相机，避免作弊
        } catch (Exception e) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("return-data", false);
        }
        return intent;
    }



    /**
     * 爱零工相机 可以连拍
     * @parm dir 如果是平常模式，会用到path，连拍模式,path自动生成
     * @parm quality 照片质量 1低 2中 3高
     * @return
     */
    public static Intent getMyCameraIntent(String dir, EvidenceRequest evidenceRequest, int takePhotoAuxiliaryLine) {//使用爱零工相机可以实现连拍
        Intent intent = new Intent();
        intent.setAction("com.slicejobs.algsdk.algtasklibrary.MY_CAMERA_KIT");
        intent.putExtra(CameraKitActivity.EVIDENCE_REQUEST, evidenceRequest);
        intent.putExtra("type", CameraKitActivity.PHOTO_TYPE);
        intent.putExtra(CameraKitActivity.PHOTO_DIR, dir);
        intent.putExtra(CameraKitActivity.PHOTO_HELP_LINE, takePhotoAuxiliaryLine);
        return intent;
    }


    /**爱零工相机拍的中等质量视频
     * 没有设置缓存,videoPath:/storage/emulated/0/Android/data/com.slicejobs.ailinggong/files/certs/Video/video20160314144613.mp4
     * ThumbnailPath:/storage/emulated/0/Android/data/com.slicejobs.ailinggong/files/certs/Thumbnail/video20160314144613.jpg
     * @return
     */
    public static Intent getMyCameraVideoIntent(String saveDir, EvidenceRequest evidenceRequest) {
        Intent intent = new Intent();
        //爱零工相机 camerakit视频相机
        intent.setAction("com.slicejobs.algsdk.algtasklibrary.MY_CAMERA_KIT");
        intent.putExtra("type", CameraKitActivity.VIDEO_TYPE);
        intent.putExtra(CameraKitActivity.EVIDENCE_REQUEST, evidenceRequest);
        intent.putExtra("dir", saveDir);

        return intent;
    }


    public static Intent getCropImageIntent(Uri photoUri, Uri outUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //intent.setClassName("com.android.camera", "com.android.camera.CropImage");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        return intent;
    }



    /**
     * 检查手机是否存某个app包
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    /**
     * 照相功能
     * uri指定照片存储路径
     */
    public static Intent cameraMethod(Context context, Uri uri) throws Exception {
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String cameraPackageName = PickImageIntentWrapper.getCameraPhoneAppInfos(context);
        if (cameraPackageName == null) {
            cameraPackageName = "com.android.camera";
        }

        final Intent intent_camera = context.getPackageManager()
                .getLaunchIntentForPackage(cameraPackageName);

        boolean canUseStytemCamera = PrefUtil.make(context, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.USER_STYTEM_CAMERA__KEY, true);

        if (intent_camera != null && canUseStytemCamera) {
            imageCaptureIntent.setPackage(cameraPackageName);
        }

        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageCaptureIntent.putExtra("return-data", false);
        return imageCaptureIntent;
    }


    /**
     * @param context
     * @return
     * @throws Exception
     * 遍历手机中所有安装的软件，

     判断软件的名称为"相机,照相机,照相,拍照,摄像,Camera,camera"等关键字，

     系统相机无非都是这样命名的
     *
     */
    public static String getCameraPhoneAppInfos(Context context) throws Exception {
        String strCamera = "";
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            try {
                PackageInfo packageInfo = packages.get(i);
                String strLabel = packageInfo.applicationInfo.loadLabel(
                        context.getPackageManager()).toString();
                // 一般手机系统中拍照软件的名字
                if ("相机,照相机,照相,拍照,摄像,Camera,camera".contains(strLabel)) {
                    strCamera = packageInfo.packageName;
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (strCamera != null) {
            return strCamera;
        }

        return null;
    }

}
