package com.slicejobs.algsdk.algtasklibrary.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskStepsWebActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;


public class FloatingRecordService extends Service {

    public static final int FLOAT_REORD_STATUS_RECORDING = 1;
    public static final int FLOAT_REORD_STATUS_PAUSE = 2;
    public static final int FLOAT_REORD_STATUS_RESUME = 3;
    public static final int FLOAT_REORD_STATUS_BACK_TO_RESUME = 4;
    private static final int TIME_SECODN_MESSAGE_WHAT = 1111;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    private TextView floatRecordTime;
    private View floatView;
    private boolean isFloatWindowShow;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            int currentRecordStatus = intent.getIntExtra(TaskStepsWebActivity.CURRENT_RECORD_STATUS_KEY, -1);
            int currentRecordTime = intent.getIntExtra(TaskStepsWebActivity.CURRENT_RECORD_TIME_KEY, 0);
            if (currentRecordStatus == FLOAT_REORD_STATUS_RECORDING) {
                showFloatingWindow(currentRecordTime);
            } else if (currentRecordStatus == FLOAT_REORD_STATUS_PAUSE) {
                handler.removeMessages(TIME_SECODN_MESSAGE_WHAT);
            } else if (currentRecordStatus == FLOAT_REORD_STATUS_RESUME) {
                if(isFloatWindowShow) {
                    Message msg = handler.obtainMessage();
                    msg.what = TIME_SECODN_MESSAGE_WHAT;
                    msg.arg1 = currentRecordTime + 1;
                    handler.sendMessage(msg);
                }
            } else if (currentRecordStatus == FLOAT_REORD_STATUS_BACK_TO_RESUME) {
                handler.removeMessages(TIME_SECODN_MESSAGE_WHAT);
                if (isFloatWindowShow && floatView != null) {
                    windowManager.removeView(floatView);
                    isFloatWindowShow = false;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFloatWindowShow && floatView != null) {
            windowManager.removeView(floatView);
            isFloatWindowShow = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow(int currentRecordTime) {
        if (Settings.canDrawOverlays(this)) {
            if (!isFloatWindowShow) {
                //自定义最小化窗口样子
                floatView = LayoutInflater.from(this).inflate(R.layout.float_record_popup, null);
                // 获取WindowManager服务
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                floatRecordTime = floatView.findViewById(R.id.float_record_time);
                floatRecordTime.setText(DateUtil.recordRockon(currentRecordTime));
                Message msg = handler.obtainMessage();
                msg.what = TIME_SECODN_MESSAGE_WHAT;
                msg.arg1 = currentRecordTime + 1;
                handler.sendMessage(msg);
                // 设置LayoutParam
                layoutParams = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                layoutParams.format = PixelFormat.RGBA_8888;
                layoutParams.width = DensityUtil.dip2px(this, 70);
                layoutParams.height = DensityUtil.dip2px(this, 70);
                layoutParams.x = DensityUtil.screenWidthInPix(this) - DensityUtil.dip2px(this, 70);
                layoutParams.y = 300;

                // 将悬浮窗控件添加到WindowManager
                windowManager.addView(floatView, layoutParams);
                floatView.setOnTouchListener(new FloatingOnTouchListener());
                isFloatWindowShow = true;
            }
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private float downX,upX;
        private float downY,upY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(floatView, layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    upX = event.getRawX();
                    upY = event.getRawY();
                    double distance = Math.sqrt(Math.abs(downX - upX) * Math.abs(downX - upX) + Math.abs(downY - upY) * Math.abs(downY - upY));//两点之间的距离
                    if (distance < 15) { // 距离较小，当作click事件来处理
                        windowManager.removeView(floatView);
                        isFloatWindowShow = false;
                        stopSelf();
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), TaskStepsWebActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent =
                                PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        return false;
                    } else {//滑动事件
                        return true;
                    }
                default:
                    break;
            }
            return false;
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TIME_SECODN_MESSAGE_WHAT) {
                int currentTime = msg.arg1;
                floatRecordTime.setText(DateUtil.recordRockon(currentTime));
                Message message = handler.obtainMessage();
                message.what = TIME_SECODN_MESSAGE_WHAT;
                message.arg1 = currentTime + 1;
                handler.sendMessageDelayed(message,1000);
            }
        }
    };
}
