package com.slicejobs.algsdk.algtasklibrary.net.oss;

import android.app.slice.Slice;
import android.content.Context;
import android.media.MediaPlayer;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.slicejobs.algsdk.algtasklibrary.BuildConfig;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.OSSTicket;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;

import java.io.IOException;
import java.util.Random;


/**
 * Created by nlmartian on 12/30/15.
 */
public class OssUploader {
    private String BUCKET_IMAGE = "sjimgpub";
    private String BUCKET_VIDEO = "sjvideopub";
    private String BUCKET_AUDIO = "sjaudiopub";

    private String PLATFORM = "algapp";

    private String TYPE_USER = "user";
    private String TYPE_ORDER = "order";

    private String endPoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private Context context;

    private OssService ossService;

    public OssUploader(Context context) {
        this.context = context;

        if (!PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean(AppConfig.IS_RELEASE, false)) {
            BUCKET_IMAGE += "dev";
            BUCKET_VIDEO += "dev";
            BUCKET_AUDIO += "dev";
        }
    }

    public void uploadUserAvatar(String userId, String path, OnUploadListener listener) {
        PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除token
        String object = PLATFORM + "/" + TYPE_USER + "/" + userId + "/" + userId + "/" + getObjectName(userId, userId);
        String url = "http://" + BUCKET_IMAGE + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_IMAGE, new UIDisplayer(url, listener));
        uploadImage(object, path);
    }

    //上传任务照片
    public OSSAsyncTask uploadTaskCert(String userId, String taskId, String path, OnUploadListener listener) {
        String object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectName(taskId, userId);
        String url = "http://" + BUCKET_IMAGE + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_IMAGE, new UIDisplayer(url, listener));
        return uploadImage(object, path);
    }

    //上传任务照片
    public OSSAsyncTask uploadTaskCert(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String userId, String taskId, String path, OnUploadListener listener) {
        String object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectName(stepIndex,stepId,isFirstCommit,isCache,taskId,userId);
        String url = "http://" + BUCKET_IMAGE + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_IMAGE, new UIDisplayer(url, listener));
        return uploadImage(object, path);
    }

    //上传任务视频
    public OSSAsyncTask uploadTaskVideoCert(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String userId, String taskId, String path, OnUploadListener listener) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        int duration = 0;
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration() / 1000;
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String object;
        if (duration != 0) {
            object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectVideoName(stepIndex,stepId,isFirstCommit,isCache,taskId, userId,duration);
        } else {
            object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectVideoName(stepIndex,stepId,isFirstCommit,isCache,taskId, userId);
        }
        String url = "http://" + BUCKET_VIDEO + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_VIDEO, new UIDisplayer(url, listener));

        return uploadLargeFile(object, path);//只有视频才启动断点续传
        //return uploadImage(object, path);
    }

    //上传任务录音
    public OSSAsyncTask uploadTaskRecordCert(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String userId, String taskId, String path, OnUploadListener listener) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        int duration = 0;
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration() / 1000;
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String object;
        if (duration != 0) {
            object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectRecordName(stepIndex, stepId, isFirstCommit, isCache, taskId, userId,duration);
        } else {
            object = PLATFORM + "/" + TYPE_ORDER + "/" + userId + "/" + taskId + "/" + getObjectRecordName(stepIndex, stepId, isFirstCommit, isCache, taskId, userId);
        }
        String url = "http://" + BUCKET_AUDIO + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_AUDIO, new UIDisplayer(url, listener));
        return uploadImage(object, path);
    }

    //上传任务json
    public OSSAsyncTask uploadTaskJsonCert(String taskId, String path, OnUploadListener listener) {
        String object = PLATFORM + "/" + TYPE_ORDER + "/" + BizLogic.getCurrentUser().userid + "/" + taskId + "/" + getObjectFileName(taskId);
        String url = "http://" + BUCKET_IMAGE + ".oss-cn-hangzhou.aliyuncs.com/" + object;
        ossService = initOSS(endPoint, BUCKET_IMAGE, new UIDisplayer(url, listener));
        return uploadImage(object, path);
    }

    private String getObjectVideoName(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String id, String userId) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object = new StringBuilder()
                .append(userId)
                .append("_")
                .append(id)
                .append("_")
                .append(stepId)
                .append("_")
                .append(stepIndex)
                .append("_")
                .append(isFirstCommit?"first":"amend")
                .append("_")
                .append(isCache?"cache":"normal")
                .append("_").append(System.currentTimeMillis()).append("_").append(end)
                .append(".mp4").toString();


        return object;
    }

    private String getObjectVideoName(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String id, String userId, int duration) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object = new StringBuilder()
                .append(userId)
                .append("_")
                .append(id)
                .append("_")
                .append(stepId)
                .append("_")
                .append(stepIndex)
                .append("_")
                .append(isFirstCommit?"first":"amend")
                .append("_")
                .append(isCache?"cache":"normal")
                .append("_")
                .append("dur" + duration)
                .append("_").append(System.currentTimeMillis()).append("_").append(end)
                .append(".mp4").toString();


        return object;
    }

    private String getObjectRecordName(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String id, String userId, int duration) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object= new StringBuilder()
                .append(userId)
                .append("_")
                .append(id)
                .append("_")
                .append(stepId)
                .append("_")
                .append(stepIndex)
                .append("_")
                .append(isFirstCommit?"first":"amend")
                .append("_")
                .append(isCache?"cache":"normal")
                .append("_")
                .append("dur" + duration)
                .append("_").append(System.currentTimeMillis()).append("_").append(end)
                .append(".mp3").toString();


        return object;
    }

    private String getObjectRecordName(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String id, String userId) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object= new StringBuilder()
                .append(userId)
                .append("_")
                .append(id)
                .append("_")
                .append(stepId)
                .append("_")
                .append(stepIndex)
                .append("_")
                .append(isFirstCommit?"first":"amend")
                .append("_")
                .append(isCache?"cache":"normal")
                .append("_").append(System.currentTimeMillis()).append("_").append(end)
                .append(".mp3").toString();


        return object;
    }



    private String getObjectName(String id, String userId) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object = new StringBuilder()
                    .append(id)
                    .append("_")
                    .append(userId)
                    .append("_").append(System.currentTimeMillis()).append("_").append(end)
                    .append(".jpeg").toString();

        return object;
    }

    private String getObjectName(int stepIndex, String stepId, boolean isFirstCommit, boolean isCache, String id, String userId) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(id);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object = new StringBuilder()
                .append(userId)
                .append("_")
                .append(id)
                .append("_")
                .append(stepId)
                .append("_")
                .append(stepIndex)
                .append("_")
                .append(isFirstCommit?"first":"amend")
                .append("_")
                .append(isCache?"cache":"normal")
                .append("_")
                .append(System.currentTimeMillis()).append("_").append(end)
                .append(".jpeg").toString();

        return object;
    }

    private String getObjectFileName(String taskId) {
        long idInteger = 0;
        try {
            idInteger = Long.parseLong(taskId);
        } catch (NumberFormatException e) {
        }
        int end = new Random().nextInt(10000000) + 10000000;

        String object = new StringBuilder()
                .append("orderResultJson.json").toString();

        return object;
    }

    /**
     * 普通上传方式，
     * @param object
     * @param path
     * @return
     */
    private OSSAsyncTask uploadImage(String object, String path) {
        return ossService.asyncPutImage(object, path);
    }

    /**
     * 大文件断点上传
     * @param object
     * @param path
     * @return
     */
    private OSSAsyncTask uploadLargeFile(String object, String path) {
        return ossService.asyncMultiPartUpload(object, path);
    }


    private OssService initOSS(String endpoint, String bucket, UIDisplayer displayer) {
        //如果希望直接使用accessKey来访问的时候，可以直接使用OSSPlainTextAKSKCredentialProvider来鉴权。
        //OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        /**
         * 由于服务器负载问题，使用直接设置StsToken
         * 主要逻辑：
         * 1: 身份证，头像，任务证据，不同入口会删除本地 oss token
         * 2: 主要上传任务证据，如扫码，分布任务（token过期识别需要涉及到本地时间和服务器时间，想想可能有坑，所以建议用户手动触发）
         */

        OSSTicket ossTicket = (OSSTicket) PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getObject(AppConfig.OSS_TOKEN_KEY, OSSTicket.class);

        OSSCredentialProvider credentialProvider;


        if (ossTicket != null) { //服务器获取一次，更新
            credentialProvider = new OSSStsTokenCredentialProvider(ossTicket.getAccessKeyId(), ossTicket.getAccessKeySecret(), ossTicket.getSecurityToken());

            /**
             * 以本地时间，或服务器时间，(坑太多，另可用户手动触发比较友好)
             */
//            try {
//                SimpleDateFormat e = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
//                e.setTimeZone(TimeZone.getTimeZone("UTC"));
//                Date date = e.parse(ossTicket.getExpiration());
//
//                if ((date.getTime() / 1000L) - (System.currentTimeMillis() / 1000L) < 300) {//过期前5分钟
//                    PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除token
//                }
//
//            } catch (ParseException var4) {
//                PrefUtil.make(CONTEXT, PrefUtil.PREFERENCE_NAME).putString(AppConfig.OSS_TOKEN_KEY, "");//删除token
//            }

        } else {
            credentialProvider = new STSGetter();
        }

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(context, endpoint, credentialProvider, conf);
        return new OssService(oss, bucket, displayer);

    }

}
