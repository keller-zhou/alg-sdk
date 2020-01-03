package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.MiniTask;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.Api;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.ui.base.PickPhotoActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by keller.zhou on 17/3/3.
 */
public class MarketInfoGatherTaskStepsActivity extends PickPhotoActivity implements IJsRenderListener {

    public static final String EXTRA_ISHAVEEVIDENCE = "extra_isHaveEvidence";
    public static final String EXTRA_TASK = "extra_task";
    public static final String EXTRA_RESULT_DATA = "extra_result_data";
    public static final String EXTRA_CHECK_LOCATION = "extra_checkinlocation";
    public static final String EXTRA_TASK_DURATION = "extra_taskDuration";
    public static final String EXTRA_INTERRUPTED_TIMES = "extra_interrupted_times";
    public static final String EXTRA_OUTRANGE_TIMES = "extra_outrange_times";
    @BindView(R2.id.tasksteps_view)
    RelativeLayout taskStepsView;
    WXSDKInstance mWXSDKInstance;

    private boolean isHaveEvidence;
    private MiniTask task;
    private String resultData;
    private String checkinlocation;
    private String taskDuration;
    private String interruptedTimes;
    private String outrangeTimes;
    public static final int CODE_UPLOAD_REQUEST = 1111;

    private StringBuilder initJsonBuild;
    private LocationClient locationClient;
    private String cacheUploadStatus = "0";//默认缓存上传
    private String marketGatherinfo = "";

    public static Intent getStartIntent(Context context, MiniTask miniTask, String resultData,
                                        String checkinlocation, String taskDuration, String interruptedTimes,
                                        String outrangeTimes, boolean isHaveEvidence) {
        Intent intent = new Intent(context, MarketInfoGatherTaskStepsActivity.class);
        intent.putExtra(EXTRA_TASK, miniTask);
        intent.putExtra(EXTRA_RESULT_DATA, resultData);
        intent.putExtra(EXTRA_CHECK_LOCATION, checkinlocation);
        intent.putExtra(EXTRA_TASK_DURATION, taskDuration);
        intent.putExtra(EXTRA_INTERRUPTED_TIMES, interruptedTimes);
        intent.putExtra(EXTRA_OUTRANGE_TIMES, outrangeTimes);
        intent.putExtra(EXTRA_ISHAVEEVIDENCE, isHaveEvidence);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasksteps_web);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        task = (MiniTask) getIntent().getSerializableExtra(EXTRA_TASK);
        resultData = getIntent().getStringExtra(EXTRA_RESULT_DATA);
        checkinlocation = getIntent().getStringExtra(EXTRA_CHECK_LOCATION);
        taskDuration = getIntent().getStringExtra(EXTRA_TASK_DURATION);
        interruptedTimes = getIntent().getStringExtra(EXTRA_INTERRUPTED_TIMES);
        outrangeTimes = getIntent().getStringExtra(EXTRA_OUTRANGE_TIMES);
        isHaveEvidence = getIntent().getBooleanExtra(EXTRA_ISHAVEEVIDENCE,false);

        Gson mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();

        User user = BizLogic.getCurrentUser();
        initJsonBuild = new StringBuilder();
        initJsonBuild.append("{");

        if (StringUtil.isNotBlank(task.getTaskid())) {
            initJsonBuild.append("\"taskId\":\"").append(task.getTaskid()).append("\"");
        }

        if (StringUtil.isNotBlank(task.getMarket_gatherinfo())) {
            initJsonBuild.append(",");
            initJsonBuild.append("\"market_gatherinfo\":").append(task.getMarket_gatherinfo());
        }

        if (StringUtil.isNotBlank(task.getOrderid())) {
            initJsonBuild.append(",");
            initJsonBuild.append("\"orderid\":\"").append(task.getOrderid()).append("\"");
        }

        if (StringUtil.isNotBlank(task.getRfversion())) {
            initJsonBuild.append(",");
            initJsonBuild.append("\"rfversion\":\"").append(task.getRfversion()).append("\"");
        }

        initJsonBuild.append("}");
        renderJs(AppConfig.MARKET_GATHER_TASK_STEPS_VIEW_FILE,initJsonBuild.toString(),"门店信息收集分步",this);

    }

    @Override
    public void onViewCreated(WXSDKInstance mWXSDKInstance, View view) {
        this.mWXSDKInstance = mWXSDKInstance;
        taskStepsView.addView(view);
    }

    @OnClick({R2.id.action_return})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            if(mWXSDKInstance != null) {
                Map<String, Object> finishParams = new HashMap<>();
                finishParams.put("eventType", "back");
                mWXSDKInstance.fireGlobalEventCallback("marketGatherEvent", finishParams);
            }
            finish();
        }
    }


    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }



    /**
     * 由h5触发接口
     * @param event
     */
    @Subscribe
    public void onTaskStepViewEvent(AppEvent.TaskStepViewEvent event) {
        if (StringUtil.isBlank(event.eventType)) {
            return;
        }else if (event.eventType.equals("marketStepUploadEvidence")) {//立即上传
            if(event.params.get("market_gatherinfo") != null){
                marketGatherinfo = event.params.get("market_gatherinfo").toString();
            }
            if(isHaveEvidence){
                Intent intent = UploadCacheActivity.getStartIntent(
                        MarketInfoGatherTaskStepsActivity.this,
                        task,
                        resultData,
                        checkinlocation,
                        taskDuration,
                        interruptedTimes,
                        outrangeTimes,
                        marketGatherinfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
                startActivityForResult(intent, CODE_UPLOAD_REQUEST);
            }else {
                startLocating(new BDAbstractLocationListener() {

                    @Override
                    public void onReceiveLocation(BDLocation bdLocation) {
                        locationClient.unRegisterLocationListener(this);
                        if (bdLocation != null) {
                            if (bdLocation.getLongitude() == 4.9E-324 || bdLocation.getLatitude() == 4.9E-324 || bdLocation.getLongitude() == 0 || bdLocation.getLatitude() == 0) {//还是没有得到经纬度,拿心跳
                                newFinishTask("0,0");
                            } else {
                                newFinishTask(bdLocation.getLongitude() + "," + bdLocation.getLatitude());
                            }
                        } else {
                            newFinishTask( "0,0");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_UPLOAD_REQUEST) {//上传界面返回（任务完成）
            setResult(RESULT_OK);
            finish();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                BusProvider.getInstance().post(new AppEvent.TaskStatusEvent(task.getTaskid(), "4"));
                setResult(RESULT_OK);
                finish();
            }
        }
    };

    private void startLocating(BDAbstractLocationListener listener) {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationClient = new LocationClient(getApplicationContext());
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(listener);
        locationClient.start();
    }

    private void newFinishTask( String finishLocation) {
        User user  = BizLogic.getCurrentUser();
        String timestamp = DateUtil.getCurrentTime();
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder signBuilder = new SignUtil.SignBuilder();
        signBuilder.put("userid", user.userid)
                .put("orderid", task.getOrderid())
                .put("templateresultjson", resultData)
                .put("cacheuploadstatus", cacheUploadStatus)
                .put("op", "finish");
        if (checkinlocation != null) {
            signBuilder.put("checkinlocation", checkinlocation);
        } else {
            signBuilder.put("checkinlocation", "0,0");
        }
        signBuilder.put("location", finishLocation);
        signBuilder.put("timestamp", timestamp);
        signBuilder.put("sec_consumed", taskDuration);
        signBuilder.put("interrupted_times", interruptedTimes);
        signBuilder.put("outrange_times", outrangeTimes);
        signBuilder.put("market_gatherinfo", marketGatherinfo);
        signBuilder.put("appId", appId);
        String sig = signBuilder.build();
        Api api = RestClient.getInstance().provideApi();
        Observable<Response<Task>> taskObservable = null;
        if (checkinlocation == null) {
            taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                    resultData, finishLocation, "0,0", cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes,marketGatherinfo,appId,sig);
        } else {
            taskObservable = api.newFinishOrder(user.userid, "finish", task.getOrderid(),
                    resultData, finishLocation, checkinlocation, cacheUploadStatus, timestamp, taskDuration, interruptedTimes, outrangeTimes,marketGatherinfo,appId,sig);
        }
        taskObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    dismissProgressDialog();
                    if (res.ret == 0) {
                        BusProvider.getInstance().post(new AppEvent.TaskStatusEvent(task.getTaskid(), "4"));
                        setResult(RESULT_OK);
                        toast(getString(R.string.upload_success));
                        finish();
                    } else {
                        toast(res.msg);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        toast("任务上传失败，请检查当前网络环境是否正常");
                        dismissProgressDialog();
                    }
                });

    }

    @Override
    public void onBackPressed() {
        if(mWXSDKInstance != null) {
            Map<String, Object> finishParams = new HashMap<>();
            finishParams.put("eventType", "back");
            mWXSDKInstance.fireGlobalEventCallback("marketGatherEvent", finishParams);
        }
        finish();
    }
}
