package com.slicejobs.algsdk.algtasklibrary.net.oss;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OSS on 2015/12/7 0007.
 * 完成显示图片，上传下载对话框显示，进度条更新等操作。
 */
public class UIDisplayer {

    private ImageView imageView;
    private ProgressBar bar;
    private TextView infoView;
    private Activity activity;

    private Handler handler;

    private static final int DOWNLOAD_OK = 1;
    private static final  int DOWNLOAD_FAIL = 2;
    private static final int UPLOAD_OK = 3;
    private static final int UPLOAD_FAIL = 4;
    private static final int UPDATE_PROGRESS = 5;
    private static final int DISPLAY_IMAGE = 6;
    private static final int DISPLAY_INFO = 7;
    private static final int SETTING_OK = 88;


    /* 必须在UI线程中初始化handler */
    public UIDisplayer(final String url, final OnUploadListener onUploadListener){

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
//                Logger.d("OSS_UPLOAD", inputMessage.what + "=>" + inputMessage.obj);
                switch (inputMessage.what) {

                    case UPLOAD_OK:
                        if (onUploadListener != null) {
                            onUploadListener.onUploadSuccess(url);
                        }
                        break;
                    case UPLOAD_FAIL:
                        if (onUploadListener != null) {
                            if (null != inputMessage.obj) {
                                onUploadListener.onUploadFail(inputMessage.obj.toString());
                            } else {
                                onUploadListener.onUploadFail("上传中断");
                            }
                        }
                        break;
                    case UPDATE_PROGRESS:
                        if (onUploadListener != null) {
                            onUploadListener.onUploadProgress(inputMessage.arg1);
                        }
                        break;
                    case DOWNLOAD_OK:
                        break;
                    case SETTING_OK:
                        break;
                    case DOWNLOAD_FAIL:
                        break;
                    case DISPLAY_IMAGE:
                        break;
                    case DISPLAY_INFO:
                        break;
                    default:
                        break;
                }
            }
        };

    }


    //下载成功，显示对应的图片
    public void downloadComplete(Bitmap bm) {
        if (null != bm) {
            displayImage(bm);
        }

        Message mes = handler.obtainMessage(DOWNLOAD_OK);
        mes.sendToTarget();
    }

    public void settingOK() {
        Message mes = handler.obtainMessage(SETTING_OK);
        mes.sendToTarget();
    }

    //下载失败，显示对应的失败信息
    public void downloadFail(String info) {
        Message mes = handler.obtainMessage(DOWNLOAD_FAIL, info);
        mes.sendToTarget();
    }

    //上传成功
    public void uploadComplete() {
        Message mes = handler.obtainMessage(UPLOAD_OK);
        mes.sendToTarget();
    }

    //上传失败，显示对应的失败信息
    public void uploadFail(String info) {
        Message mes = handler.obtainMessage(UPLOAD_FAIL, info);
        mes.obj = info;
        mes.sendToTarget();
    }

    //更新进度，取值范围为[0,100]
    public void updateProgress(int progress) {
        //Log.d("UpdateProgress", String.valueOf(progress));
        if (progress > 100) {
            progress = 100;
        }
        else if (progress < 0) {
            progress = 0;
        }

        Message mes = handler.obtainMessage(UPDATE_PROGRESS, progress);
        mes.arg1 = progress;
        mes.sendToTarget();
    }

    //显示图像
    public void displayImage(Bitmap bm) {
        Message mes = handler.obtainMessage(DISPLAY_IMAGE, bm);
        mes.sendToTarget();
    }

    //在主界面输出文字信息
    public void displayInfo(String info) {
        Message mes = handler.obtainMessage(DISPLAY_INFO, info);
        mes.sendToTarget();
    }

    //根据ImageView的大小自动缩放图片
    public Bitmap autoResizeFromLocalFile(String picturePath) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
        //options.inSampleSize = 10;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picturePath, options);

    }

    //根据ImageView大小自动缩放图片
    public Bitmap autoResizeFromStream(InputStream stream) throws IOException {

        byte[] data;
        {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = stream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            data = outStream.toByteArray();
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
        //options.inSampleSize = 10;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }


    //计算图片缩放比例
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
