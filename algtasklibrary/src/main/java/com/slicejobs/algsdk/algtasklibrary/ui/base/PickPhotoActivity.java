package com.slicejobs.algsdk.algtasklibrary.ui.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.EvidenceRequest;
import com.slicejobs.algsdk.algtasklibrary.model.Photo;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepResult;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.SelectTaskPhotoActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ActionSheetDialog;
import com.slicejobs.algsdk.algtasklibrary.utils.CameraUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.ImageUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PickImageIntentWrapper;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by martian on 14-07-09.
 * Base Activity for choosing photos from gallery or camera
 */

@RuntimePermissions
public class PickPhotoActivity extends BaseActivity {
    protected static File PHOTO_DIR;
    public static File UPLOAD_AVATAR;
    protected static File CROPED_AVATAR;
    protected Uri mUriTemp;
    protected Uri mCropedTemp;
    protected static final int CAMERA_WITH_DATA = 3023;//相机拍照
    protected static final int PHOTO_PICKED_WITH_DATA = 3021;//图库
    protected static final int PHOTO_CROPED_WITH_DATA = 3024;
    protected static final int CAMERA_WITH_VIDEO = 3025;//相机拍video
    protected static final int CAMERA_WITH_RECORD = 3026;//录音record
    protected static final int MYCAMERT_WITH_DATA = 3027;//使用自己的相机
    protected static final int OPENCV_CAMERT_WITH_DATA = 3028;//使用全景相机
    protected static final int VIDEO_PICKED_WITH_DATA = 3029;//本地视频
    protected static final int RECORD_PICKED_WITH_DATA = 3030;//本地录音
    protected static final int TASK_PHOTO_PICKED_WITH_DATA = 3031;//图库
    protected static final int MULTI_PHOTO_PICKED_WITH_DATA = 3032;//图库多选

    protected String currRecordPath;

    protected String evidenceDir;//保存证据的路径

    public interface OnImageProcessedListener {

        void onImageProcessed(Photo photo);
    }

    protected Photo photo;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PHOTO_DIR = this.getExternalCacheDir();
            if (PHOTO_DIR == null) {
                PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "/slicejobs/");
                if (!PHOTO_DIR.exists()) {
                    PHOTO_DIR.mkdirs();
                }
            }
            UPLOAD_AVATAR = new File(PHOTO_DIR, "upload_avatar.jpeg");
            CROPED_AVATAR = new File(PHOTO_DIR, "croped_avatar.jpeg");
            mUriTemp =  Uri.fromFile(UPLOAD_AVATAR);
            mCropedTemp = Uri.fromFile(CROPED_AVATAR);
        } else {
            //Toast.makeText(this, r.string.text_no_sdcard, Toast.LENGTH_SHORT).show();
            toast(getResources().getString(R.string.text_no_sdcard));
        }

        evidenceDir =  this.getIntent().getStringExtra("cache_dir");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void doGetPhotoAction(boolean forceCamera, boolean allowReusePhoto, String resultData, String dir, EvidenceRequest evidenceRequest, int takePhotoAuxiliaryLine) {
        if (forceCamera && !allowReusePhoto) {//如果只允许能拍照又不允许复用,直接拍照
            openCamera(dir, evidenceRequest, takePhotoAuxiliaryLine);
            return;
        }
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(PickPhotoActivity.this).builder();
        actionSheetDialog.setCancelable(true);
        actionSheetDialog.setCanceledOnTouchOutside(true);
        actionSheetDialog.addSheetItem("拍摄", ActionSheetDialog.SheetItemColor.ThemeColor,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openCamera(dir, evidenceRequest, takePhotoAuxiliaryLine);
                            }
                        });
        if(allowReusePhoto){
            actionSheetDialog.addSheetItem("使用已拍摄的图片", ActionSheetDialog.SheetItemColor.Black,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            doPickPhotoFromTaskGallery(resultData);
                        }
                    });
        }
        if(!forceCamera){
            actionSheetDialog.addSheetItem("从手机相册选择", ActionSheetDialog.SheetItemColor.Black,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            doPickPhotoFromGallery();
                        }
                    });
        }
        actionSheetDialog.show();
    }

    //打开相机，默认爱零工相机
    public void openCamera(String dir, EvidenceRequest evidenceRequest, int takePhotoAuxiliaryLine) {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            if(StringUtil.isNotBlank(evidenceRequest.getQuality()) && evidenceRequest.getQuality().equals(EvidenceRequest.PHOTO_QUALITY_HIGHEST)){
                MobclickAgent.onEvent(SliceApp.CONTEXT, "um_function_use_stytem_camera");
                doTakePhoto(dir);//系统相机
            }else {
                int currCamera = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.CAMERA_TYPE, AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG);
                if (currCamera == AppConfig.SERVICE_PHOTO_CAMERA_SELECT_ALG
                        || currCamera == AppConfig.LOCAL_PHOTO_CAMERA_SELECT_ALG) {
                    MobclickAgent.onEvent(SliceApp.CONTEXT, "um_function_use_slicejobs_camera");
                    doMyTaskPhoto(dir, evidenceRequest, takePhotoAuxiliaryLine);//爱零工相机
                } else {
                    MobclickAgent.onEvent(SliceApp.CONTEXT, "um_function_use_stytem_camera");
                    doTakePhoto(dir);//系统相机
                }
            }
        } else {
            //Toast.makeText(PickPhotoActivity.this, r.string.text_no_sdcard, Toast.LENGTH_SHORT).show();
            toast(getResources().getString(R.string.text_no_sdcard));
        }
    }

    public void doGetPhotoUploadOrUse(boolean forceCamera, boolean allowReusePhoto, String resultData, String dir, EvidenceRequest evidenceRequest) {

        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(PickPhotoActivity.this).builder();
        actionSheetDialog.setCancelable(true);
        actionSheetDialog.setCanceledOnTouchOutside(true);
        if(allowReusePhoto){
            actionSheetDialog.addSheetItem("使用已拍摄的图片", ActionSheetDialog.SheetItemColor.Black,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            doPickPhotoFromTaskGallery(resultData);
                        }
                    });
        }
        if(!forceCamera){
            actionSheetDialog.addSheetItem("从手机相册选择", ActionSheetDialog.SheetItemColor.Black,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            doPickPhotoFromGallery();
                        }
                    });
        }
        actionSheetDialog.show();
    }

    public void doGetVideoAction(String dir, EvidenceRequest evidenceRequest) {

        new ActionSheetDialog(PickPhotoActivity.this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("拍摄", ActionSheetDialog.SheetItemColor.ThemeColor,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                String status = Environment.getExternalStorageState();
                                if (status.equals(Environment.MEDIA_MOUNTED)) {
                                    cacheTaskVideo(dir, evidenceRequest);
                                } else {
                                    //Toast.makeText(PickPhotoActivity.this, r.string.text_no_sdcard, Toast.LENGTH_SHORT).show();
                                    toast(getResources().getString(R.string.text_no_sdcard));
                                }
                            }
                        })
                .addSheetItem("从手机相册选择", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                doPickVideoFromGallery();
                            }
                        }).show();
        /*showHintDialog(new BaseActivity.DialogClickLinear() {
                           @Override
                           public void cancelClick() {//拍照
                               String status = Environment.getExternalStorageState();
                               if (status.equals(Environment.MEDIA_MOUNTED)) {
                                   int currCamera = PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.CAMERA_TYPE, 1);
                                   if (currCamera == 0) {
                                       doMyTaskPhoto(dir, evidenceRequest);//爱零工相机
                                   } else {
                                       doTakePhoto();//系统相机
                                   }
                               } else {
                                   Toast.makeText(PickPhotoActivity.this, r.string.text_no_sdcard, Toast.LENGTH_SHORT).show();
                               }
                           }

                           @Override
                           public void defineClick() {//图库
                               doPickPhotoFromGallery();
                           }
                       }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "请选择图片来源", SliceApp.CONTEXT.getString(R.string.text_take_photo),
                SliceApp.CONTEXT.getString(R.string.text_pick_photo), true);*/
    }

    /**
     * 使用系统相机
     */
    protected void doTakePhoto() {
        PickPhotoActivityPermissionsDispatcher.openSystemCameraWithCheck(this);
    }

    /**
     * 使用系统相机
     */
    protected void doTakePhoto(String evidenceDir) {
        this.evidenceDir = evidenceDir;
        PickPhotoActivityPermissionsDispatcher.openSystemCameraWithCheck(this);
    }

    //打开系统相机，权限检查
    @NeedsPermission(Manifest.permission.CAMERA)
    void openSystemCamera() {
        try {
            final Intent intent = PickImageIntentWrapper
                    .getTakePickIntent(mUriTemp);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, r.string.text_no_camera,
//                    Toast.LENGTH_SHORT).show();
            toast(getResources().getString(R.string.text_no_camera));
        }
    }

    //第一次请求权限被用户拒绝，下次请求权限之前会调用,
    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(PermissionRequest request) {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "相机被禁用", "请在手机“设置-应用程序权限管理-选择爱零工”允许启动摄像头或相机", "以后再说", "打开", false);
    }

    //用户点击拒绝权限时调用
    @OnPermissionDenied(Manifest.permission.CAMERA)
    void onCameraDenied() {

    }

    //用户点击不再询问这个权限时调用
    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onCameraNeverAskAgain() {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "相机被禁用", "请在手机“设置-应用程序权限管理-选择爱零工”允许启动摄像头或相机", "以后再说", "打开", false);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PickPhotoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    /**
     * 调用Slicejobs自己相机,实现连拍
     * @param dir 连拍模式,照片目录
     * @param evidenceRequest 照片要求
     */
    protected void doMyTaskPhoto(String dir, EvidenceRequest evidenceRequest, int takePhotoAuxiliaryLine) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){//低于6.0系统判断相机权限是否打开
            if(CameraUtil.isCameraUseable()){//相机能正常打开
                openSlicejobsCamera(dir, evidenceRequest,takePhotoAuxiliaryLine);
            }else {
                showHintDialog(new DialogThreeClickLinear() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void middleClick() {
                        openSystemCamera();
                    }

                    @Override
                    public void defineClick() {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    }
                }, "相机被禁用", "相机被禁用,请在手机“设置-应用程序权限管理-选择爱零工”允许启动摄像头或相机", "取消", "设置","使用系统相机", false);
            }
        }else {
            PickPhotoActivityPermissionsDispatcher.openSlicejobsCameraWithCheck(this, dir, evidenceRequest,takePhotoAuxiliaryLine);
        }
    }

    //爱零工相机权限检查
    @NeedsPermission(Manifest.permission.CAMERA)
    void openSlicejobsCamera(String dir, EvidenceRequest evidenceRequest, int takePhotoAuxiliaryLine) {
        try {
            final Intent intent = PickImageIntentWrapper.getMyCameraIntent(dir, evidenceRequest,takePhotoAuxiliaryLine);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
            startActivityForResult(intent, MYCAMERT_WITH_DATA);
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, r.string.text_no_camera,
//                    Toast.LENGTH_SHORT).show();
            toast(getResources().getString(R.string.text_no_camera));
        }
    }


    /**
     * 上传视频
     * @param dir
     */
    protected  void cacheTaskVideo(String dir, EvidenceRequest evidenceRequest) {
        PickPhotoActivityPermissionsDispatcher.openSlicejobsVideoWithCheck(this, dir, evidenceRequest);
    }

    /**
     * 检查录制视频的敏感权限
     * @param dir
     * @param evidenceRequest
     */
    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    void openSlicejobsVideo(String dir, EvidenceRequest evidenceRequest) {
        try {
            final Intent intent = PickImageIntentWrapper.getMyCameraVideoIntent(dir, evidenceRequest);
            startActivityForResult(intent, CAMERA_WITH_VIDEO);
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, r.string.text_no_camera,
//                    Toast.LENGTH_SHORT).show();
            toast(getResources().getString(R.string.text_no_camera));
        }
    }

    //用户点击不再询问这个权限时调用（实际上用户禁止权限会调用）
    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    void onVideoNeverAskAgain() {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "摄像头或麦克风被禁用", "请在手机“设置-应用程序权限管理-选择爱零工”允许启动（麦克风和摄像头）或（录音和拍照）", "以后再说", "打开", false);
    }



    //线上bug,无读取sd卡权限6.0手机crash
    protected void doPickPhotoFromGallery() {
        PickPhotoActivityPermissionsDispatcher.openPickPhotoFromGalleryWithCheck(this);
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void openPickPhotoFromGallery() {
        Matisse.from(this).choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(15)
                .gridExpectedSize(DensityUtil.dip2px(this, 100))//图片显示表格的大小getResources()
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Dracula)//主题  暗色主题 R2.style.Matisse_Dracula
                .imageEngine(new GlideEngine())
                .forResult(MULTI_PHOTO_PICKED_WITH_DATA);
    }

    //线上bug,无读取sd卡权限6.0手机crash
    protected void doPickSinglePhotoFromGallery() {
        PickPhotoActivityPermissionsDispatcher.openPickSinglePhotoFromGalleryWithCheck(this);
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void openPickSinglePhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = PickImageIntentWrapper.getPhotoPickIntent(mUriTemp);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {

        }
    }

    //线上bug,无读取sd卡权限6.0手机crash
    protected void doPickPhotoFromTaskGallery(String resultData) {
        PickPhotoActivityPermissionsDispatcher.openPickPhotoFromTaskGalleryWithCheck(this,resultData);
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void openPickPhotoFromTaskGallery(String resultData) {
        if (isHasEvidencePhotoReuse(resultData)) {
            try {
                final Intent intent = SelectTaskPhotoActivity.getStartIntent(this, resultData);
                startActivityForResult(intent, TASK_PHOTO_PICKED_WITH_DATA);
            } catch (ActivityNotFoundException e) {

            }
        } else {
            showHintDialog(new DialogDefineClick() {
                @Override
                public void defineClick() {

                }
            }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "当前暂无已拍摄照片", "我知道了", true);
        }
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onPickPhotoFromGallery() {
        showHintDialog(new DialogClickLinear() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "读取手机相册被禁止", "请在手机“设置-应用程序权限管理-选择爱零工”允许存储", "以后再说", "打开", false);
    }

    //线上bug,无读取sd卡权限6.0手机crash
    protected void doPickVideoFromGallery() {
        PickPhotoActivityPermissionsDispatcher.openPickVideoFromGalleryWithCheck(this);
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void openPickVideoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = PickImageIntentWrapper.getVideoPickIntent();
            startActivityForResult(intent, VIDEO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {

        }
    }

    //线上bug,无读取sd卡权限6.0手机crash
    protected void doPickRecordFromGallery() {
        PickPhotoActivityPermissionsDispatcher.openPickRecordFromGalleryWithCheck(this);
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void openPickRecordFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = PickImageIntentWrapper.getRecordPickIntent();
            startActivityForResult(intent, RECORD_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {

        }
    }




    /**
     * 处理图片旋转并压缩大小(手机自带相机)
     * @param uri
     * @param listener
     * @param evidenceRequest 证据要求
     */
    protected void processPhoto(final Uri uri, final OnImageProcessedListener listener, EvidenceRequest evidenceRequest) {
        showProgressDialog();
        try {
            ExifInterface ei = new ExifInterface(uri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Observable.create(subscriber -> {
                try {
                    photo = ImageUtil.compressImageForUri(this,uri, evidenceRequest, evidenceDir, orientation);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object obj) {
                            listener.onImageProcessed(photo);
                            PickPhotoActivity.this.dismissProgressDialog();
                        }
                    }, e -> {
                        dismissProgressDialog();
                        toast(SliceApp.CONTEXT.getString(R.string.text_image_fail));
                        MobclickAgent.reportError(SliceApp.CONTEXT, "图片压缩失败：用户id：" + BizLogic.getCurrentUser().userid + "失败原因:" + e.getMessage() + e.getCause());
                    });

        } catch (IOException e) {
            e.printStackTrace();
            dismissProgressDialog();
        }
    }

    /**
     * 处理图片旋转并压缩大小(手机自带相机)
     * @param bytes
     * @param listener
     * @param evidenceRequest 证据要求
     * @param isHiddenCamera 是否在暗拍模式下
     */
    protected void processPhoto(final byte[] bytes, final OnImageProcessedListener listener, EvidenceRequest evidenceRequest, float orientation,boolean isHiddenCamera) {
        //如果暗拍模式下，不显示dialog
        if(!isHiddenCamera){
            //showProgressDialog();
        }

        try {

            Observable.create(subscriber -> {
                try {
                    photo = ImageUtil.compressImageForBites(this,bytes, evidenceRequest, evidenceDir, orientation);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object obj) {
                            listener.onImageProcessed(photo);
                            PickPhotoActivity.this.dismissProgressDialog();
                        }
                    }, e -> {
                        dismissProgressDialog();
                        MobclickAgent.reportError(SliceApp.CONTEXT, "图片压缩失败：用户id：" + BizLogic.getCurrentUser().userid + "失败原因:" + e.getMessage() + e.getCause());

                        if (e instanceof NullPointerException) {

                        } else {
                            toast(SliceApp.CONTEXT.getString(R.string.text_image_fail));
                        }



                    });

        } catch (Exception e) {
            e.printStackTrace();
            dismissProgressDialog();
        }

    }



    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * 没有读取内存卡权限，这里6.0会crash
     *
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public interface OnIsAgainPickPhoto {

        public void cancelPick();

        public void definePick();

    }

    private DisplayImageOptions localImageDisplayOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(false)
            .build();


    private boolean isHasEvidencePhotoReuse (String resultJson) {
        if(StringUtil.isNotBlank(resultJson)){
            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
            List<TaskStepResult> resultList = gson.fromJson(
                    resultJson, new TypeToken<List<TaskStepResult>>() {
                    }.getType());

            for (int i = 0; i < resultList.size(); i++) {
                TaskStepResult result = resultList.get(i);
                List<String> photos = result.getPhotos();
                if(photos != null && photos.size() != 0){
                    return true;
                }
            }
        }
        return false;
    }
}
