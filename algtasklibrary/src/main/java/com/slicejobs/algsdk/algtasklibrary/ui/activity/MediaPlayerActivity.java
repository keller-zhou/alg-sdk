package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

public class MediaPlayerActivity extends AppCompatActivity {

    @BindView(R2.id.videoplayer)
    JZVideoPlayerStandard jzVideoPlayerStandard;
    @BindView(R2.id.resource_record_flag)
    ImageView recordFlag;

    private String url;
    private SensorManager mSensorManager;
    private JZVideoPlayer.JZAutoFullscreenListener mSensorEventListener;

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, MediaPlayerActivity.class);
        intent.putExtra("url", url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_play);
        ButterKnife.bind(this);
        url = getIntent().getStringExtra("url");
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensorManager != null && mSensorEventListener != null) {
            Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(mSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initView(){
        if(StringUtil.isNotBlank(url)) {
            jzVideoPlayerStandard.setUp(url, JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,"");
            jzVideoPlayerStandard.startVideo();
            if(url.endsWith("mp3")){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
                jzVideoPlayerStandard.fullscreenButton.setVisibility(View.GONE);
                recordFlag.setVisibility(View.VISIBLE);
            }else {
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mSensorEventListener = new JZVideoPlayer.JZAutoFullscreenListener();
            }
        }
    }

    @OnClick({R2.id.action_return})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(JZVideoPlayerStandard.backPress()){
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mSensorManager != null && mSensorEventListener != null) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
        JZVideoPlayerStandard.releaseAllVideos();
    }
}
