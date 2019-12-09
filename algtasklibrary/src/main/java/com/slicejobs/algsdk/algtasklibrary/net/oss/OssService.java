package com.slicejobs.algsdk.algtasklibrary.net.oss;

import android.graphics.Bitmap;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by oss on 2015/12/7 0007.
 * 支持普通上传，普通下载和断点上传
 */
public class OssService {

    private OSS oss;
    private String bucket;
    private UIDisplayer UIDisplayer;
    private MultiPartUploadManager multiPartUploadManager;
    private String callbackAddress;
    //根据实际需求改变分片大小
    private final static int partSize = 256 * 1024;

    public OssService(OSS oss, String bucket, UIDisplayer UIDisplayer) {
        this.oss = oss;
        this.bucket = bucket;
        this.UIDisplayer = UIDisplayer;
        this.multiPartUploadManager = new MultiPartUploadManager(oss, bucket, partSize, UIDisplayer);
    }

    public void SetBucketName(String bucket) {
        this.bucket = bucket;
    }

    public void InitOss(OSS _oss) {
        this.oss = _oss;
    }

    public void setCallbackAddress(String callbackAddress) {
        this.callbackAddress = callbackAddress;
    }

    public void asyncGetImage(String object) {
        if ((object == null) || object.equals("")) {
            Log.w("AsyncGetImage", "ObjectNull");
            return;
        }

        GetObjectRequest get = new GetObjectRequest(bucket, object);

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                InputStream inputStream = result.getObjectContent();
                //重载InputStream来获取读取进度信息
                ProgressInputStream progressStream = new ProgressInputStream(inputStream, new OSSProgressCallback<GetObjectRequest>() {
                    @Override
                    public void onProgress(GetObjectRequest o, long currentSize, long totalSize) {
                        Log.d("GetObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                        int progress = (int) (100 * currentSize / totalSize);
                        UIDisplayer.updateProgress(progress);
                        UIDisplayer.displayInfo("下载进度: " + String.valueOf(progress) + "%");
                    }
                }, result.getContentLength());

                //Bitmap bm = BitmapFactory.decodeStream(inputStream);
                try {
                    //需要根据对应的View大小来自适应缩放
                    Bitmap bm = UIDisplayer.autoResizeFromStream(progressStream);
                    UIDisplayer.downloadComplete(bm);
                    UIDisplayer.displayInfo("Bucket: " + bucket + "\nObject: " + request.getObjectKey() + "\nRequestId: " + result.getRequestId());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                UIDisplayer.downloadFail(info);
                UIDisplayer.displayInfo(info);
            }
        });
    }


    public OSSAsyncTask asyncPutImage(String object, String localFile) {
        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return null;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncPutImage", "FileNotExist");
            Log.w("LocalFile", localFile);
            return null;
        }

        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);

        if (PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.IS_MD5_OSSVERIFI, 1) == 1){

            //md5验证
            ObjectMetadata metadata = new ObjectMetadata();

            byte[] b = new byte[0];
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                b = new byte[(int)file.length()];     //创建合适文件大小的数组
                in.read(b);    //读取文件中的内容到b[]数组
                metadata.setContentMD5(BinaryUtil.calculateBase64Md5(b)); // 如果是上传二进制数据
                b = null;
                put.setMetadata(metadata);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {//出现异常，取消md5验证
                Log.e("slicejbos","ioexception cancle md5 verifi");
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (callbackAddress != null) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", callbackAddress);
                    //callbackBody可以自定义传入的信息
                    put("callbackBody", "filename=${object}");
                }
            });
        }

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                int progress = (int) (100 * currentSize / totalSize);
                UIDisplayer.updateProgress(progress);
                UIDisplayer.displayInfo("上传进度: " + String.valueOf(progress) + "%");
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                UIDisplayer.uploadComplete();
                UIDisplayer.displayInfo("Bucket: " + bucket
                        + "\nObject: " + request.getObjectKey()
                        + "\nETag: " + result.getETag()
                        + "\nRequestId: " + result.getRequestId()
                        + "\nCallback: " + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                UIDisplayer.uploadFail(info);
                UIDisplayer.displayInfo(info);
            }
        });
        return task;
    }

    //断点上传(暂不支持在本地持久保存断点记录)
    public OSSAsyncTask asyncMultiPartUpload(String object, String localFile) {
        if (object.equals("")) {
            Log.w("AsyncMultiPartUpload", "ObjectNull");
            return null;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncMultiPartUpload", "FileNotExist");
            Log.w("LocalFile", localFile);
            return null;
        }

        //Log.d("MultiPartUpload", localFile);
        //PauseableUploadTask task = multiPartUploadManager.asyncUpload(object, localFile);

        // 创建断点上传请求
        ResumableUploadRequest request = new ResumableUploadRequest(bucket, object, localFile);

        if (callbackAddress != null) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            request.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", callbackAddress);
                    //callbackBody可以自定义传入的信息
                    put("callbackBody", "filename=${object}");
                }
            });
        }

        // 设置上传过程回调
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                Log.d("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize);
                int progress = (int) (100 * currentSize / totalSize);
                UIDisplayer.updateProgress(progress);
                UIDisplayer.displayInfo("上传进度: " + String.valueOf(progress) + "%");

            }
        });

        // 异步调用断点上传
        OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
            @Override
            public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                UIDisplayer.uploadComplete();
                UIDisplayer.displayInfo("Bucket: " + bucket
                        + "\nObject: " + request.getObjectKey()
                        + "\nETag: " + result.getETag()
                        + "\nRequestId: " + result.getRequestId()
                        + "\nCallback: " + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(ResumableUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                UIDisplayer.uploadFail(info);
                UIDisplayer.displayInfo(info);
            }
        });

        return resumableTask;
    }

}
