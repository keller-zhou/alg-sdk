package com.slicejobs.algsdk.algtasklibrary.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czt.mp3recorder.Mp3Recorder;
import com.czt.mp3recorder.Mp3RecorderUtil;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.EvidenceRequest;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.WebviewActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StatusBarUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

import static com.czt.mp3recorder.Mp3Recorder.ACTION_STOP_AND_NEXT;

/**
 * Created by keller.zhou on 16/3/16.
 * 考虑到极少数部分手机系统，不带录音功能，so自定义
 */
public class ALGRecordFragment extends BaseFragment {

    private static final int RECORD_TIME_WHAT = 1111;
    private static final int RECORD_STOP = 2222;
    @BindView(R2.id.action_minimize)
    LinearLayout actionMinimize;
    @BindView(R2.id.tv_recording)
    TextView tvRecording;
    @BindView(R2.id.tv_record_time)
    TextView tvRecordTime;
    @BindView(R2.id.tv_record_notice1)
    TextView tvRecordNotice1;
    @BindView(R2.id.tv_record_notice2)
    TextView tvRecordNotice2;
    @BindView(R2.id.tv_record_notice3)
    TextView tvRecordNotice3;
    @BindView(R2.id.bt_hide_screen)
    TextView btHideScreen;
    @BindView(R2.id.bt_start_record)
    Button btStartRecord;
    @BindView(R2.id.recore_again)
    TextView btRecordAgain;
    @BindView(R2.id.finish_record)
    Button btFinishRecord;
    @BindView(R2.id.frame_overlay)
    FrameLayout mOvrtlay;
    @BindView(R2.id.minimize_layout)
    FrameLayout minimizeLayout;
    @BindView(R2.id.minimize_recordtime_text)
    TextView minimizeRecordTime;
    @BindView(R2.id.minimize_recordstatus)
    TextView minimizeRecordStatus;
    @BindView(R2.id.minimize_record_stop)
    TextView minimizeRecordStop;
    @BindView(R2.id.title_layout)
    FrameLayout titleLayout;
    @BindView(R2.id.record_layout)
    FrameLayout recordLayout;
    @BindView(R2.id.float_window_notice)
    FrameLayout floatWindowNotice;
    @BindView(R2.id.allowCrossSubjectRecordLayout)
    LinearLayout allowCrossSubjectRecordLayout;

    public static final String SOURCE_RECORD = "record";
    public static final String SOURCE_PLAY = "play";

    public static final int RESUNT_RECORD_OK = 8888;

    private String source;//来源

    private boolean isRecorder = false;//是否证在录音
    private boolean isRecorderEnd = false;//是否录音完成

    public int currentRecordTime = 0 ;
    private boolean stopReturn;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == RECORD_TIME_WHAT){
                currentRecordTime = msg.arg1;
                tvRecordTime.setText(DateUtil.recordRockon(currentRecordTime));
                minimizeRecordTime.setText(DateUtil.recordRockon(currentRecordTime));
            }else if(msg.what == RECORD_STOP){
                minimizeRecordStatus.setText("录音已结束");
                minimizeRecordStop.setVisibility(View.GONE);
                if (stopReturn) {
                    algRecordFinishListener.onRecordFinish(true,stepId);
                }
            }
        }
    };

    private String cacheDir;//临时缓存文件路径
    private String filePath;
    private String stepId;
    private boolean allowCrossSubjectRecord;

    public static final String EVIDENCE_REQUEST = "evidenceRequest";//证据要求

    private EvidenceRequest evidenceRequest;

    //是否是在暗拍模式下
    private boolean isHiddenCamera;
    private Mp3Recorder mp3Recorder;
    /**
     * 传入fragment的参数
     */
    protected Bundle fragmentArgs;
    private AlgRecordFinishListener algRecordFinishListener;
    private View algRecordView;
    private boolean isMini;

    public void setAlgRecordFinishListener(AlgRecordFinishListener algRecordFinishListener) {
        this.algRecordFinishListener = algRecordFinishListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        algRecordView = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, algRecordView);
        Mp3RecorderUtil.init(getActivity(),false);
        fragmentArgs = getArguments();
        source = fragmentArgs.getString("source");//目标目录
        filePath = fragmentArgs.getString("filePath");
        stepId = fragmentArgs.getString("stepId");
        allowCrossSubjectRecord = fragmentArgs.getBoolean("allowCrossSubjectRecord", false);
        evidenceRequest = (EvidenceRequest) fragmentArgs.getSerializable(EVIDENCE_REQUEST);
        mp3Recorder = new Mp3Recorder();
        mp3Recorder.setOutputFile(filePath)
                .setCallback(callback);
        File file = new File(filePath);
        if (file.exists()) {
        }

        if(Build.VERSION.SDK_INT >= 28) {
            tvRecordNotice3.setVisibility(View.VISIBLE);
        }
        algRecordView.setClickable(true);
        if (allowCrossSubjectRecord) {
            allowCrossSubjectRecordLayout.setVisibility(View.VISIBLE);
        } else {
            allowCrossSubjectRecordLayout.setVisibility(View.GONE);
        }
        return algRecordView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getActivity())) {
                floatWindowNotice.setVisibility(View.VISIBLE);
            }else {
                floatWindowNotice.setVisibility(View.GONE);
            }
        }else {
            floatWindowNotice.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp3Recorder.reset();
    }

    @OnClick({R2.id.bt_start_record, R2.id.action_return,R2.id.bt_hide_screen,R2.id.frame_overlay_arct_exit,R2.id.recore_again,R2.id.finish_record,R2.id.action_minimize,R2.id.minimize_layout,R2.id.minimize_record_stop,
        R2.id.open_float_window,R2.id.allowCrossSubjectRecordLayout})
    public void OnClick(View view) {
        if (view.getId() == R.id.action_return) {
            if(isRecorder) {//录制中
                showHintDialog(new DialogClickLinear() {
                                   @Override
                                   public void cancelClick() {

                                   }

                                   @Override
                                   public void defineClick() {
                                       algRecordFinishListener.onRecordFinish(false,stepId);
                                   }
                               }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "当前正在录音，你确定退出吗？",
                        SliceApp.CONTEXT.getString(R.string.cancel), SliceApp.CONTEXT.getString(R.string.confirm), false);
            }else {
                if(isRecorderEnd){//录制完成
                    algRecordFinishListener.onRecordFinish(true,stepId);
                }else {
                    algRecordFinishListener.onRecordFinish(false,stepId);
                }
            }
        } else if (view.getId() == R.id.bt_start_record) {
            if (StringUtil.isNotBlank(source) && source.equals(SOURCE_RECORD)) {//开始录音
                if (!isRecorder) {//开始录音
                    clickStartRecord();

                } else if(isRecorder) {//停止录音

                    clickStopRecord();

                }

            } else if (StringUtil.isNotBlank(source) && source.equals(SOURCE_PLAY)) {//用户播放录音

            }
        } else if (view.getId() == R.id.bt_hide_screen) {
            isHiddenCamera = true;
            mOvrtlay.setVisibility(View.VISIBLE);
            StatusBarUtil.setWindowStatusBarColor(getActivity(),R.color.color_black);
        } else if (view.getId() == R.id.frame_overlay_arct_exit) {
            mOvrtlay.setVisibility(View.GONE);
            isHiddenCamera = false;
            StatusBarUtil.setWindowStatusBarColor(getActivity(),R.color.color_white);
        } else if (view.getId() == R.id.recore_again) {
            showHintDialog(new DialogClickLinear() {
                               @Override
                               public void cancelClick() {

                               }

                               @Override
                               public void defineClick() {//重新录制
                                   clickStartRecord();
                               }
                           }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), SliceApp.CONTEXT.getString(R.string.hint_record_again),
                    "取消", "确定", false);
        } else if (view.getId() == R.id.finish_record) {
            algRecordFinishListener.onRecordFinish(true,stepId);
        } else if (view.getId() == R.id.action_minimize) {
            minimizeLayout.setVisibility(View.VISIBLE);
            titleLayout.setVisibility(View.GONE);
            recordLayout.setVisibility(View.GONE);
            algRecordFinishListener.onRecordFloatint(true);
            algRecordView.setClickable(false);
            isMini = true;
        } else if (view.getId() == R.id.minimize_layout) {
            minimizeLayout.setVisibility(View.GONE);
            titleLayout.setVisibility(View.VISIBLE);
            recordLayout.setVisibility(View.VISIBLE);
            algRecordFinishListener.onRecordFloatint(false);
            algRecordView.setClickable(true);
            isMini = false;
        } else if (view.getId() == R.id.minimize_record_stop) {
            stopReturn = true;
            clickStopRecord();
        } else if (view.getId() == R.id.open_float_window) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName())), 0);
        } else if (view.getId() == R.id.allowCrossSubjectRecordLayout) {
            startActivity(WebviewActivity.getStartIntent(getActivity(), AppConfig.webHost.getAppWebHost()+"/public/cross_subject_record_explain.html"));
        }

    }

    public void clickStartRecord() {
        if (evidenceRequest.getRecordDuration() != 0) {
            showHintDialog(new DialogDefineClick() {
                @Override
                public void defineClick() {
                    mp3Recorder.start();
                }
            }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "这个录音至少需要录制"+DateUtil.castMinut(evidenceRequest.getRecordDuration()), "我知道了", false);
        } else {
            mp3Recorder.start();
        }
        minimizeRecordStatus.setText("正在录音中");
        minimizeRecordStop.setVisibility(View.VISIBLE);
    }

    public void clickStopRecord() {

        if (evidenceRequest.getRecordDuration() != 0 && evidenceRequest.getRecordDuration() > currentRecordTime) {
            if (stopReturn) {
                stopReturn = false;
            }
            showHintDialog(new DialogDefineClick() {
                @Override
                public void defineClick() {


                }
            }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint),  "你还差" + DateUtil.castMinut(evidenceRequest.getRecordDuration() - currentRecordTime) + "才能结束哦", "我知道了", false);
        } else {
            if (isMini) {
                showHintDialog(new DialogClickLinear() {
                                   @Override
                                   public void cancelClick() {

                                   }

                                   @Override
                                   public void defineClick() {
                                       mp3Recorder.stop(ACTION_STOP_AND_NEXT);
                                   }
                               }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "当前正在录音，你确定退出吗？",
                        SliceApp.CONTEXT.getString(R.string.cancel), SliceApp.CONTEXT.getString(R.string.confirm), false);
            }else {
                mp3Recorder.stop(ACTION_STOP_AND_NEXT);
            }
        }
    }

    private Mp3Recorder.Callback callback = new Mp3Recorder.Callback() {
        @Override
        public void onStart() {
            isRecorder = true;
            isRecorderEnd = false;
            algRecordFinishListener.onRecording(isRecorder,isRecorderEnd);
            tvRecordNotice1.setVisibility(View.VISIBLE);
            tvRecordNotice1.setText("点击“隐藏屏幕”，可以进行黑屏录音");
            tvRecordNotice2.setVisibility(View.VISIBLE);
            tvRecording.setVisibility(View.VISIBLE);
            btHideScreen.setVisibility(View.VISIBLE);
            btRecordAgain.setVisibility(View.GONE);
            btFinishRecord.setVisibility(View.GONE);
            tvRecordTime.setTextColor(getResources().getColor(R.color.color_base));
            btStartRecord.setVisibility(View.VISIBLE);
            btStartRecord.setText(R.string.stop_record);
            actionMinimize.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPause() {
            algRecordFinishListener.onRecordPause();
        }

        @Override
        public void onResume() {
            algRecordFinishListener.onRecordResume();
        }

        @Override
        public void onStop(int i) {
            isRecorder = false;
            isRecorderEnd = true;
            algRecordFinishListener.onRecording(isRecorder,isRecorderEnd);
            btStartRecord.setVisibility(View.GONE);
            btHideScreen.setVisibility(View.GONE);
            tvRecordTime.setTextColor(getResources().getColor(R.color.text_color7));
            btRecordAgain.setVisibility(View.VISIBLE);
            btFinishRecord.setVisibility(View.VISIBLE);
            tvRecordNotice1.setVisibility(View.GONE);
            tvRecordNotice2.setVisibility(View.GONE);
            handler.sendEmptyMessage(RECORD_STOP);
            actionMinimize.setVisibility(View.GONE);
        }

        @Override
        public void onReset() {

        }

        @Override
        public void onRecording(double duration, double volume) {
            Message msg = Message.obtain();
            msg.what = RECORD_TIME_WHAT;
            msg.arg1 = (int) (duration / 1000);
            handler.sendMessage(msg);
        }

        @Override
        public void onMaxDurationReached() {

        }
    };

    public interface AlgRecordFinishListener{
        void onRecording(boolean isRecorder, boolean isRecorderEnd);
        void onRecordFinish(boolean ifSaveRdcord, String stepId);
        void onRecordFloatint(boolean ifFloating);
        void onRecordPause();
        void onRecordResume();
    }

    public void pauseRecord(){
        if(mp3Recorder != null){
            if(mp3Recorder.getRecorderState() == Mp3Recorder.State.RECORDING){
                mp3Recorder.pause();
            }
        }
    }

    public void resumeRecord(){
        if(mp3Recorder != null){
            if(mp3Recorder.getRecorderState() == Mp3Recorder.State.PAUSED){
                mp3Recorder.resume();
            }
        }
    }
}
