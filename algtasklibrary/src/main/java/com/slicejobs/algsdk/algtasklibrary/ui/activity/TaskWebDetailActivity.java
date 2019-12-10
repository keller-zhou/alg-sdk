package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.model.ShareBean;
import com.slicejobs.algsdk.algtasklibrary.model.TaskMoreMenuItem;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.Api;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.TaskMoreMenuAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXBaseEventModule;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by keller.zhou on 9/15/15.
 */
public class TaskWebDetailActivity extends BaseActivity implements IJsRenderListener {

    public static final String TO_MYTASK_ACTION = "to_mytask_action";
    private static final int TO_FACE_DETECTION_REQUEST_CODE = 111;
    @BindView(R2.id.web_task_detail_layout)
    RelativeLayout webTaskDetailView;
    @BindView(R2.id.action_more)
    LinearLayout actionMore;
    @BindView(R2.id.task_more_menu)
    ListView taskMoreMenuView;
    @BindView(R2.id.rootView)
    FrameLayout rootView;
    private List<TaskMoreMenuItem> taskMoreMenuItemList = new ArrayList<>();
    private TaskMoreMenuAdapter taskMoreMenuAdapter;

    WXSDKInstance mWXSDKInstance;
    public static final int REQUEST_CODE_CACHE_STEP = 2;//进入分布 缓存上传
    public static final int REQUEST_CODE_CACHE_UPLOAD = 3;//进入上传页面
    public static final int REQUEST_CODE_LAST_UPLOAD_STEP = 4;//进入分布 最后一步上传
    private ShareBean shareBean;
    private User user = BizLogic.getCurrentUser();

    //通过全局,进行更新
    private Map<String, Object> params = new HashMap<>();

    private AlertDialog countdownDialog;//定义子类特有属性
    private SurfaceHolder surfaceHolder;

    private StringBuilder initJsonData = null;
    ProgressBar progressBar;
    private TextView qiangdanStatus;
    private MyCountDownTimer mc;
    private boolean isMoreMenuOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_taskdetail);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        Bundle bundle = getIntent().getExtras();
        SerializableBaseMap serializableBaseMap = (SerializableBaseMap) bundle.get("weex_data");
        Map<String, Object> data = serializableBaseMap.getMap();
        String sourceData  = data.get("initData").toString();

        initJsonData = new StringBuilder();
        initJsonData.append(sourceData.substring(0, sourceData.length()-1));

        //判断开启缓存上传模式
        int uploadType = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getInt(AppConfig.UPLOAD_TYPE, 0);

        if (uploadType == 1) {
            initJsonData.append(",");
            initJsonData.append("\"isOpenCacheUpload\":").append(true);
        }

        initJsonData.append("}");

        renderJs(AppConfig.TASK_DETAIL_VIEW_FILE,initJsonData.toString(),"任务详情",this);
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        webTaskDetailView.addView(view);
        actionMore.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
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

    /**
     * 由h5触发接口
     * @param event
     */
    @Subscribe
    public void ontaskDetailViewEvent(AppEvent.TaskDetailViewEvent event) {
        if (StringUtil.isBlank(event.eventType)) {
            return;
        } else if (event.eventType.equals("startOrder")) {//开始按钮
            startOrder(event);
        }  else if (event.eventType.equals("openImagebrowser")) {//点击图片
            ArrayList<String> urlList = new ArrayList<>();
            ArrayList<String> urlListNew = new ArrayList<>();
            if(event.params != null){
                urlList.addAll((Collection<? extends String>) event.params.get("photos"));
                if(urlList != null && urlList.size() != 0){
                    int position = (int) event.params.get("index");
                    for(int i=0;i<urlList.size();i++){
                        String mUrl = urlList.get(i);
                        urlListNew.add(mUrl);
                    }
                    startActivity(ViewImageActivity.getIntent(this, urlListNew, position));
                }
            }
        } else if (event.eventType.equals("showVieTaskProgress")) {//显示抢单进度条-------3.2.0
            showNewCountdownDialog();
        } else if (event.eventType.equals("hideVieTaskProgress")) {//关闭抢单进度条
            if(event.params.get("ret") != null){
                if((Integer)event.params.get("ret") == 0){
                    if(progressBar != null){
                        progressBar.setProgress(progressBar.getMax());
                        mc.cancel();
                    }
                    if(qiangdanStatus != null){
                        qiangdanStatus.setText("抢单成功");
                    }
                    handler.sendEmptyMessageDelayed(1, 500);

                }else{
                    dismissNewCountDownDialog();
                }
            }else{
                dismissNewCountDownDialog();
            }
        } else if (event.eventType.equals("popTaskDetail")) {//退出detail
            //读取参数，看看是否刷新否个任务列表
            Map<String, Object> map = event.params;
            if (null != map && StringUtil.isNotBlank("openSource")) {
                //home_task_list首页, store_task_list门店赚, nearby_task_list周边赚, country_task_list轻松赚, map_task_list地图任务, my_task_list我的任务
                BusProvider.getInstance().post(new AppEvent.RefreshTaskEvent(map.get("openSource").toString()));
            }

            finish();

        }  else if (event.eventType.equals("showProgress")) {//打开普通进度条
            TaskWebDetailActivity.this.showProgressDialog();
        } else if (event.eventType.equals("hideProgress")) {//关闭普通进度条
            TaskWebDetailActivity.this.dismissProgressDialog();
        } else if (event.eventType.equals("updateNativeMoreMenu")) {//更新更多菜单
            taskMoreMenuItemList.clear();
            String taskEvaluate = null;
            boolean isShowTaskPreview = true;
            boolean isShowShare = false;
            boolean isStoreTask = false;
            if (event.params.get("showPreview") != null) {
                isShowTaskPreview = (boolean) event.params.get("showPreview");
            }
            if (event.params.get("checkEvaluate") != null) {
                taskEvaluate = event.params.get("checkEvaluate").toString();
            }
            if (event.params.get("showShared") != null) {
                isShowShare = (boolean) event.params.get("showShared");
            }
            if (event.params.get("isStoreTask") != null) {
                isStoreTask = (boolean) event.params.get("isStoreTask");
            }
            if (isShowTaskPreview) {
                taskMoreMenuItemList.add(new TaskMoreMenuItem("taskPreview", R.drawable.ic_task_preview, "任务预览"));
            }
            /*if (isShowShare) {
                taskMoreMenuItemList.add(new TaskMoreMenuItem("share",R.drawable.ic_task_share,"分享任务"));
            }*/
            if (isStoreTask) {
                taskMoreMenuItemList.add(new TaskMoreMenuItem("routePlan",R.drawable.ic_route_plan,"路线规划"));
            }
            if (StringUtil.isNotBlank(taskEvaluate)) {
                taskMoreMenuItemList.add(new TaskMoreMenuItem("taskEvaluate",R.drawable.ic_star_gray,taskEvaluate));
            }
            String giveUpTask = null;
            if (event.params.get("giveUpTask") != null) {
                giveUpTask = event.params.get("giveUpTask").toString();
            }
            if (StringUtil.isNotBlank(giveUpTask)) {
                taskMoreMenuItemList.add(new TaskMoreMenuItem("giveUpTask",R.drawable.ic_give_up_task,giveUpTask));
            }
            taskMoreMenuAdapter = new TaskMoreMenuAdapter(this,taskMoreMenuItemList);
            taskMoreMenuView.setAdapter(taskMoreMenuAdapter);
            taskMoreMenuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TaskMoreMenuItem taskMoreMenuItem = taskMoreMenuItemList.get(position);
                    if (taskMoreMenuItem.getTag().equals("taskPreview")) {
                        if (mWXSDKInstance != null) {
                            params.put("updateType", "taskPreview");
                            mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                        } else {
                            toast("请耐心等待任务详情加载成功后，再预览任务");
                        }
                    } else if (taskMoreMenuItem.getTag().equals("taskEvaluate")) {
                        if (mWXSDKInstance != null) {
                            params.put("updateType", "checkEvaluate");
                            mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                        }
                    } /*else if (taskMoreMenuItem.getTag().equals("share")) {
                        if (mWXSDKInstance != null) {
                            params.put("updateType", "taskShare");
                            mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                        }
                    } */else if (taskMoreMenuItem.getTag().equals("routePlan")) {
                        if (mWXSDKInstance != null) {
                            params.put("updateType", "openMapView");
                            mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                        }
                    } else if (taskMoreMenuItem.getTag().equals("giveUpTask")) {
                        if (mWXSDKInstance != null) {
                            params.put("updateType", "giveUpTask");
                            mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                        }
                    }
                    ScaleAnimation zoomOutSa = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.8f, Animation.RELATIVE_TO_SELF, 0);
                    zoomOutSa.setDuration(300);
                    zoomOutSa.setFillAfter(true);
                    zoomOutSa.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rootView.setVisibility(View.GONE);
                            isMoreMenuOpen = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    taskMoreMenuView.startAnimation(zoomOutSa);
                }
            });
        } else if (event.eventType.equals("goBackHomePage")) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        } else if (event.eventType.equals("taskEvaluateFinished")) {
            if (mWXSDKInstance != null) {
                params.put("updateType", "refreshEvalStatus");
                mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
            }
        }
    }

    private void startOrder(AppEvent.TaskDetailViewEvent event) {
        Intent intent = new Intent(this, TaskStepsWebActivity.class);
        Bundle bundle = new Bundle();
        SerializableBaseMap tmpmap=new SerializableBaseMap();
        tmpmap.setMap(event.params);
        bundle.putSerializable("weex_data", tmpmap);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
        startActivityForResult(intent, REQUEST_CODE_LAST_UPLOAD_STEP);


    }


    public void showNewCountdownDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_my_qiangdan, null);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        qiangdanStatus = (TextView) view.findViewById(R.id.tv_msg);
        progressBar.setMax(11);
        progressBar.setProgress(0);
        mc = new MyCountDownTimer(11000,1000);
        mc.start();
        //builder.setView(view);
        builder.setCancelable(false);
        countdownDialog = builder.create();
        countdownDialog.show();
        Window dialogWindow = countdownDialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    public void dismissNewCountDownDialog() {
        if (countdownDialog != null) {
            countdownDialog.dismiss();
            countdownDialog = null;
        }
    }

    class MyCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         *                          表示以毫秒为单位 倒计时的总数
         *      					例如 millisInFuture=1000 表示1秒
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         *                          表示 间隔 多少微秒 调用一次 onTick 方法
         *      					例如: countDownInterval =1000 ; 表示每1000毫秒调用一次onTick()
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(progressBar != null){
                progressBar.setProgress((int) ((11000 - millisUntilFinished) / 1000));
            }
        }

        @Override
        public void onFinish() {
            //dismissNewCountDownDialog();
        }
    }



    @OnClick({R2.id.action_return, R2.id.action_more,R2.id.rootView})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            finish();
        } else if (view.getId() == R.id.action_more) {
            rootView.setVisibility(View.VISIBLE);
            ScaleAnimation zoomInSa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.8f, Animation.RELATIVE_TO_SELF, 0);
            zoomInSa.setDuration(300);
            zoomInSa.setFillAfter(true);
            taskMoreMenuView.startAnimation(zoomInSa);
            isMoreMenuOpen = true;
        } else if (view.getId() == R.id.rootView) {
            ScaleAnimation zoomOutSa = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.8f, Animation.RELATIVE_TO_SELF, 0);
            zoomOutSa.setDuration(300);
            zoomOutSa.setFillAfter(true);
            zoomOutSa.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rootView.setVisibility(View.GONE);
                    isMoreMenuOpen = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            taskMoreMenuView.startAnimation(zoomOutSa);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CACHE_UPLOAD) {//缓存上传，直接进入上传页面(从上传页面返回)

            } else if (requestCode == REQUEST_CODE_LAST_UPLOAD_STEP) {//立即上传
                //通知web任务完成
                if (mWXSDKInstance != null) {
                    if (data != null) {
                        String updateType = data.getStringExtra("updateType");
                        if (StringUtil.isNotBlank(updateType)) {
                            params.put("updateType", updateType);
                        }
                    } else {
                        params.put("updateType", "taskCommitSuccess");
                    }
                    mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                }
                BusProvider.getInstance().post(new AppEvent.RefreshTaskEvent("my_task_list"));//刷新一下任务列表
            } else if (requestCode == WXBaseEventModule.LIVE_ACTIVITY_REQUEST_CODE) {
                if(mWXSDKInstance != null) {
                    params.put("updateType", "all");
                    mWXSDKInstance.fireGlobalEventCallback("taskDetailChange", params);
                }
                BusProvider.getInstance().post(new AppEvent.RefreshTaskEvent("my_task_list"));//刷新一下任务列表
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1)
                dismissNewCountDownDialog();
        }
    };


}
