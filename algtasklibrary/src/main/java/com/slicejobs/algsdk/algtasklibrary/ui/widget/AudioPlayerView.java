package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.utils.DialogUtils;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.TimeUtils;

import java.io.IOException;

/**
 * Created by jgzhu on 10/10/14.
 */
public class AudioPlayerView extends FrameLayout {

    private LayoutInflater layoutInflater;
    private ImageView audioImage;
    private SeekBar playSeekbar;
    private TextView playCurrentTime;
    private TextView playTotalTime;
    private MediaPlayer mediaPlayer;
    private int totalDuration;
    private int currentPosition;
    private boolean isStartPlay;
    private boolean isPausePlay;
    private Thread playThread;
    private boolean isPlayThreadStop;
    private boolean audioPlayEnable;
    private String audioSource;

    public void setAudioPlayEnable(boolean audioPlayEnable) {
        this.audioPlayEnable = audioPlayEnable;
    }

    public AudioPlayerView(Context context) {
        this(context, null);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.view_audio_player, this);
        audioImage = (ImageView) findViewById(R.id.iv_audio_play);
        playSeekbar = (SeekBar) findViewById(R.id.audio_play_progress);
        playCurrentTime = (TextView) findViewById(R.id.player_current_time);
        playTotalTime = (TextView) findViewById(R.id.player_total_time);
        mediaPlayer = new MediaPlayer();
        audioImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioPlayEnable) {
                    if (!isStartPlay) {
                        startPlay();
                    } else {
                        pausePlay();
                    }
                }else {
                    DialogUtils.showHintDialog(context, new DialogUtils.DialogDefineClick() {
                        @Override
                        public void defineClick() {

                        }
                    }, "", "暂不允许录制音频与播放音频同时进行。", "我知道了", true);
                }
            }
        });
        playSeekbar.setEnabled(false);
        playSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    freshProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setMediaDataSource(String dataSource){
        if(StringUtil.isNotBlank(dataSource)){
            audioSource = dataSource;
            try {
                isPlayThreadStop = true;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(dataSource);
                mediaPlayer.prepareAsync();
                isStartPlay = false;
                audioImage.setBackgroundResource(R.drawable.ic_audio_play);
                audioImage.setEnabled(false);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        totalDuration = mediaPlayer.getDuration();
                        String totalTime = TimeUtils.getTime(totalDuration);
                        playTotalTime.setText(totalTime);
                        playSeekbar.setMax(totalDuration);
                        playSeekbar.setProgress(0);
                        playCurrentTime.setText("00:00");
                        audioImage.setEnabled(true);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPlay(){
        if(isPausePlay) {
            mediaPlayer.start();
            audioImage.setBackgroundResource(R.drawable.ic_audio_pause);
            isStartPlay = true;
            playThread = new Thread(new MuiscThread());
            playThread.start();
            isPlayThreadStop = false;
            isPausePlay = false;
        }else {
            if (StringUtil.isNotBlank(audioSource)) {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(audioSource);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            audioImage.setBackgroundResource(R.drawable.ic_audio_pause);
                            isStartPlay = true;
                            playThread = new Thread(new MuiscThread());
                            playThread.start();
                            isPlayThreadStop = false;
                            isPausePlay = false;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void pausePlay(){
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            audioImage.setBackgroundResource(R.drawable.ic_audio_play);
            isStartPlay = false;
            if(mediaPlayer.getCurrentPosition() != 0) {
                isPausePlay = true;
            }else {
                isPausePlay = false;
            }
        }
    }

    public void exitPlay(){
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                isPlayThreadStop = true;
                playThread = null;
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            audioImage.setBackgroundResource(R.drawable.ic_audio_play);
            isStartPlay = false;
        }
    }

    public void freshProgress(int currentPosition){
        if(currentPosition < totalDuration) {
            playSeekbar.setProgress(currentPosition);
            String currentTime = TimeUtils.getTime(currentPosition);
            playCurrentTime.setText(currentTime);
        }else {
            isStartPlay = false;
            audioImage.setBackgroundResource(R.drawable.ic_audio_play);
        }
    }
    class MuiscThread implements Runnable {

        @Override
        //实现run方法
        public void run() {
            //判断音乐的状态，在不停止与不暂停的情况下向总线程发出信息
            while (!isPlayThreadStop && mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                    // 每100毫秒更新一次位置
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentPosition = msg.what;
            freshProgress(currentPosition);
        }
    };

}
