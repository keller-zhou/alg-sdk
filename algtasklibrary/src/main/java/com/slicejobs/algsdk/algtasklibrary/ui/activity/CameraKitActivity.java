package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.EvidenceRequest;
import com.slicejobs.algsdk.algtasklibrary.model.Photo;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.MyCameraPhotosAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.PickPhotoActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.wonderkiln.camerakit.ui.CameraFragment;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by nlmartian on 9/15/15.
 */
public class CameraKitActivity extends PickPhotoActivity implements CameraFragment.CameraKitResultListener,CameraFragment.CameraKitHideCaptureListener,CameraFragment.CameraKitHideCaptureNoticeListener{
    public static final String EVIDENCE_REQUEST = "evidenceRequest";//证据要求
    public static final String PHOTO_TYPE = "photo";
    public static final String VIDEO_TYPE = "video";
    public static final String PHOTO_DIR = "cache_dir";//dirPhoto
    public static final String PHOTO_HELP_LINE = "takePhotoAuxiliaryLine";//dirPhoto
    public static final String TEMP_VIDEO_PHOTO_KEY = "thumbnailPath";
    public static final String TEMP_VIDEO_KEY = "videoPath";
    public static final String TEMP_VIDEO_PHOTO_LIST_KEY = "thumbnailPathList";
    public static final String TEMP_VIDEO_LIST_KEY = "videoPathList";
    @BindView(R2.id.my_camera_photos)
    RecyclerView myCameraPhptos;
    @BindView(R2.id.iv_camera_help_line)
    ImageView ivCameraHelpLine;
    private EvidenceRequest evidenceRequest;
    private ArrayList<String> list = new ArrayList<>();
    private String resultDir;
    private CameraFragment cameraFragment;
    MyCameraPhotosAdapter myCameraPhotosAdapter;
    private Vibrator vibrator;
    private String cameraType = "photo";
    private int takePhotoAuxiliaryLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camerakit);
        ButterKnife.bind(this);
        cameraFragment = new CameraFragment();
        evidenceRequest = (EvidenceRequest) getIntent().getSerializableExtra(EVIDENCE_REQUEST);
        resultDir = getIntent().getStringExtra("dir");
        cameraType = getIntent().getStringExtra("type");
        takePhotoAuxiliaryLine = getIntent().getIntExtra(PHOTO_HELP_LINE, 0);

        //根据类型加载
        if(cameraType.equals(VIDEO_TYPE)) {//拍视频
            cameraFragment.setCameraType(CameraFragment.CAMERA_TYPE_VIDEO);
            if(evidenceRequest != null) {
                cameraFragment.setVideoQuality(evidenceRequest.getQuality());
                cameraFragment.setVideoDuration(evidenceRequest.getVideoDuration());
            }
        }else {
            cameraFragment.setCameraType(CameraFragment.CAMERA_TYPE_IMAGE);
            if(evidenceRequest != null) {
                cameraFragment.setPhotoQuality(evidenceRequest.getQuality());
            }
            initWidgets();
        }
        if(resultDir != null) {
            cameraFragment.setResultDir(resultDir);
        }
        cameraFragment.setCameraKitResultListener(this);
        cameraFragment.setCameraKitHideCaptureListener(this);
        cameraFragment.setCameraKitHideCaptureNoticeListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.camera_view, cameraFragment).commit();
    }

    private void initWidgets() {
        myCameraPhptos.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        myCameraPhotosAdapter = new MyCameraPhotosAdapter();
        myCameraPhptos.setAdapter(myCameraPhotosAdapter);
        if(takePhotoAuxiliaryLine != 0){
            ivCameraHelpLine.setVisibility(View.VISIBLE);
            if(takePhotoAuxiliaryLine == 1){//地推
                ivCameraHelpLine.setBackgroundResource(R.drawable.ic_camera_line_dd);
                cameraFragment.setCameraHintText("拍摄时，请将地堆边角与直线对齐");
            }else if(takePhotoAuxiliaryLine == 2){//包柱
                ivCameraHelpLine.setBackgroundResource(R.drawable.ic_camera_line_bz);
                cameraFragment.setCameraHintText("拍摄时，请将包柱边角与直线对齐");
            }else{
                ivCameraHelpLine.setBackgroundResource(R.drawable.ic_camera_line_nomal);
                if(takePhotoAuxiliaryLine == 3) {
                    cameraFragment.setCameraHintText("拍摄时，请将端架边角与直线对齐");
                }else if(takePhotoAuxiliaryLine == 4){
                    cameraFragment.setCameraHintText("拍摄时，请将主货架边角与直线对齐");
                }else if(takePhotoAuxiliaryLine == 5){
                    cameraFragment.setCameraHintText("拍摄时，请将立柜边角与直线对齐");
                }
            }
        }else {
            ivCameraHelpLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetPicture(byte[] jpeg) {

        int orientation = 1;
        if (StringUtil.isNotBlank(getIntent().getStringExtra(PHOTO_DIR))) {

            processPhoto(jpeg, new PickPhotoActivity.OnImageProcessedListener() {

                @Override
                public void onImageProcessed(Photo photo) {

                    if (photo.getIsClarity() == 0) {
                        if(photo.getImageRecognitionTypeMatch() == 0){//不开启图像识别或者符合要求
                            toast("保存成功");
                            if(!cameraFragment.isHiddenCamera()) {
                                myCameraPhptos.setVisibility(View.VISIBLE);
                            }
                            myCameraPhotosAdapter.addPath(photo.getNativeThumbnailPath());

                            list.add(photo.getNativePhotoPath());

                            Observable.timer(400, TimeUnit.MILLISECONDS, Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(aLong -> {
                                        myCameraPhptos.scrollToPosition(myCameraPhotosAdapter.getItemCount() - 1);
                                    }, e -> {

                                    });
                            if(cameraFragment.isHiddenCamera()) {
                                if (vibrator == null) {
                                    vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                }
                                vibrator.vibrate(300);
                            }
                        }

                    }
                }
            }, evidenceRequest, orientation,cameraFragment.isHiddenCamera());
        }
    }

    @Override
    public void onGetVideo(String mVideoPath, String thumbnailPath) {
        Intent intent = new Intent();//将2个路径返回去
        intent.putExtra(TEMP_VIDEO_PHOTO_KEY, thumbnailPath);
        intent.putExtra(TEMP_VIDEO_KEY, mVideoPath);
        setResult(RESULT_OK, intent);  //直接返回RESULT_OK
        finish();   //直接返回RESULT_OK
    }

    @Override
    public void onGetVideoList(ArrayList mVideoPath, ArrayList thumbnailPath) {
        Intent intent = new Intent();//将2个路径返回去
        intent.putStringArrayListExtra(TEMP_VIDEO_PHOTO_LIST_KEY, thumbnailPath);
        intent.putStringArrayListExtra(TEMP_VIDEO_LIST_KEY, mVideoPath);
        setResult(RESULT_OK, intent);  //直接返回RESULT_OK
        finish();   //直接返回RESULT_OK
    }

    @Override
    public void onCameraExit() {
        Intent intent = new Intent();
        if(cameraFragment.getCameraType() == CameraFragment.CAMERA_TYPE_IMAGE) {
            intent.putStringArrayListExtra("resultPhotos", list);
        }
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    public void onHideCaptureStart() {
        if(cameraType.equals(PHOTO_TYPE)) {
            if (myCameraPhotosAdapter.getItemCount() != 0) {
                myCameraPhptos.setVisibility(View.GONE);
            }
            ivCameraHelpLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHideCaptureExit() {
        if(cameraType.equals(PHOTO_TYPE)) {
            if (myCameraPhotosAdapter.getItemCount() != 0) {
                myCameraPhptos.setVisibility(View.VISIBLE);
            }
            if(takePhotoAuxiliaryLine != 0) {
                ivCameraHelpLine.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 监听手机返回键
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cameraFragment.exitPage();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){//音量-
            /*if(cameraType.equals(PHOTO_TYPE)) {
                if (cameraFragment.isHiddenCamera()) {
                    cameraFragment.exitHideCapture();
                    return true;
                }
            }*/
            if(cameraType.equals(PHOTO_TYPE)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    if (event.getRepeatCount() == 0) {
                        cameraFragment.capturePhoto();
                    }
                }
                return true;
            }else if (cameraType.equals(VIDEO_TYPE)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    if (event.getRepeatCount() == 0) {
                        if (cameraFragment.isCapturingVideo()) {
                            cameraFragment.clickStopRecord();
                        }else {
                            cameraFragment.captureVideo();
                        }
                    }
                }
                return true;
            }
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){//音量+
            /*if(cameraType.equals(PHOTO_TYPE)) {
                if (cameraFragment.isHiddenCamera()) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        event.startTracking();
                        if (event.getRepeatCount() == 0) {
                            cameraFragment.capturePhoto();
                        }
                    }
                    return true;
                }
            }*/
            if(cameraType.equals(PHOTO_TYPE)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    if (event.getRepeatCount() == 0) {
                        cameraFragment.capturePhoto();
                    }
                }
                return true;
            }else if (cameraType.equals(VIDEO_TYPE)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    if (event.getRepeatCount() == 0) {
                        if (cameraFragment.isCapturingVideo()) {
                            cameraFragment.clickStopRecord();
                        }else {
                            cameraFragment.captureVideo();
                        }
                    }
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            return true;
        }else{

        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onHideCaptureNoticeOpen() {
        if(cameraType.equals(PHOTO_TYPE)) {
            if (myCameraPhotosAdapter.getItemCount() != 0) {
                myCameraPhptos.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onHideCaptureNoticeClose() {
        if(cameraType.equals(PHOTO_TYPE)) {
            if (myCameraPhotosAdapter.getItemCount() != 0) {
                myCameraPhptos.setVisibility(View.VISIBLE);
            }
        }
    }
}
