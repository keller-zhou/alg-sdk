package com.slicejobs.algsdk.algtasklibrary.ui.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.Api;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskWebDetailActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.TaskListAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ActionSheetDialog;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.refresh.SwipeRefreshLayout;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.OpenLocalMapUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.squareup.otto.Subscribe;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MapTaskListFragment extends BaseFragment {
    private static final long ANIMATION_DURATION = 200;
    private static String SRC = "com.slicejobs.ailinggong";
    private static final int REQUEST_CODE_MAP_NAVI = 999;
    private static final String NAVI_REVIEW_COMMENT1 = "唤起地图软件失败";
    private static final String NAVI_REVIEW_COMMENT2 = "导航出错";
    private static final String NAVI_REVIEW_COMMENT3 = "地址不存在";
    private static final String NAVI_REVIEW_COMMENT4 = "任务地址与店名不符";
    private static final String NAVI_REVIEW_COMMENT5 = "唤起地图软件失败";
    private static final String NAVI_REVIEW_COMMENT6 = "没有常用地图软件";
    private static final String NAVI_REVIEW_COMMENT7 = "其他";
    @BindView(R2.id.map_bg)
    View background;
    @BindView(R2.id.map_dialog)
    View dialog;
    @BindView(R2.id.map_task_list)//用于显示地图上面的任务
            RecyclerView rvNearTaskList;
    @BindView(R2.id.map_action_result)//显示几条结果
            TextView tvMapResult;
    @BindView(R2.id.map_market_layout)
    LinearLayout mapMarketLayout;
    @BindView(R2.id.market_address)
    TextView tvMarketAddress;
    @BindView(R2.id.map_bottom_bar)
    FrameLayout mapBottomBar;
    @BindView(R2.id.map_guide_layout)
    LinearLayout mapGuideLayout;
    @BindView(R2.id.layout_tasks_select)
    LinearLayout tasksStatusLayout;
    @BindView(R2.id.text_not_pick)
    TextView textNotPick;
    @BindView(R2.id.line_not_pick)
    TextView lineNotPick;
    @BindView(R2.id.text_picked)
    TextView textPicked;
    @BindView(R2.id.line_picked)
    TextView linePicked;
    @BindView(R2.id.addToPlan)
    TextView addToPlanText;
    @BindView(R2.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    public Boolean flag = false;//记录当前fragment状态,默认打开
    private float downY;
    private float upY;
    TaskListAdapter taskListAdapter;
    View view;



    private int nearNunber = 0;

    private MapChangeEventInterFace mapChangeEventInterFace;
    private LatLng latLng;
    private String naviAddress;
    private String naviMarketId = "";
    private AlertDialog naviReviewDialog;
    private int currentSelectIndex;
    private ArrayList<Task> myTaskList;
    private ArrayList<Task> notPickTaskList;
    private MarketRoutePlanListener marketRoutePlanListener;
    private List<LatLng> routePlanList;
    private int addOrRemovePlan = 0;//0加入规划 1移出规划
    private boolean onRoutePlan;

    public void setMarketRoutePlanListener(MarketRoutePlanListener marketRoutePlanListener) {
        this.marketRoutePlanListener = marketRoutePlanListener;
    }

    public boolean isOnRoutePlan() {
        return onRoutePlan;
    }

    public void setOnRoutePlan(boolean onRoutePlan) {
        this.onRoutePlan = onRoutePlan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maptask_list, container, false);
        ButterKnife.bind(this, view);
        initWidget();
        view.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initWidget() {
        taskListAdapter = new TaskListAdapter(TaskListAdapter.TYPE_MAP);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvNearTaskList.setLayoutManager(mLayoutManager);
        rvNearTaskList.addItemDecoration(new MyItemDecoration());
        rvNearTaskList.setAdapter(taskListAdapter);

        taskListAdapter.setCallback((view, task, position) -> {
            StringBuilder initJsonBuild = new StringBuilder();
            initJsonBuild.append("{");
            initJsonBuild.append("\"taskId\":\"").append(task.getTaskid()).append("\"");
            initJsonBuild.append(",");
            if(StringUtil.isNotBlank(task.getOrderid())){
                initJsonBuild.append("\"orderid\":\"").append(task.getOrderid()).append("\"");
                initJsonBuild.append(",");
            }
            initJsonBuild.append("\"rfversion\":\"").append(task.getRfversion()).append("\"");
            initJsonBuild.append(",");
            initJsonBuild.append("\"openSource\":\"").append("map_task_list").append("\"");
            initJsonBuild.append("}");
            Intent intent = new Intent(getActivity(), TaskWebDetailActivity.class);
            Bundle bundle = new Bundle();

            SerializableBaseMap tmpmap=new SerializableBaseMap();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("initData", initJsonBuild.toString());
            tmpmap.setMap(params);
            bundle.putSerializable("weex_data", tmpmap);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        rvNearTaskList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {//滑动到顶部,才能刷新
                    refreshLayout.setEnabled(true);
                } else {
                    refreshLayout.setEnabled(false);
                }
                if(mapGuideLayout.getVisibility() == View.GONE) {
                    RecyclerView.LayoutManager layoutManager = rvNearTaskList.getLayoutManager();
                    int lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {//上啦刷新
                        if (mapChangeEventInterFace != null) {
                            mapChangeEventInterFace.refreshTask(-100);
                        }
                    }
                }else {
                    refreshLayout.setEnabled(false);
                }
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            if(mapGuideLayout.getVisibility() == View.GONE) {
                if(currentSelectIndex == 0) {
                    if (mapChangeEventInterFace != null) {
                        mapChangeEventInterFace.refreshTask(0);
                    }
                }else {
                    refreshLayout.setRefreshing(false);
                }
            }else {
                refreshLayout.setRefreshing(false);
            }
        });

        myTaskList = new ArrayList<>();
        notPickTaskList = new ArrayList<>();
        routePlanList = new ArrayList<LatLng>();
    }



   public static MapTaskListFragment newInstance(MapChangeEventInterFace mapChangeEventInterFace,MarketRoutePlanListener marketRoutePlanListener) {
       MapTaskListFragment fragment = new MapTaskListFragment();
       fragment.mapChangeEventInterFace = mapChangeEventInterFace;
       fragment.marketRoutePlanListener = marketRoutePlanListener;
       return fragment;
   }

    public void dismiss() {
        flag = false;


        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(background, "alpha", 1, 1);
            fadeOut.setDuration(ANIMATION_DURATION);
            fadeOut.start();
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(background, "y",
                0,
                getActivity().findViewById(android.R.id.content).getMeasuredHeight());
            slideUp.setDuration(ANIMATION_DURATION);
            slideUp.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    try {
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    } catch (Exception e) {
                    }
                }
            });
        slideUp.start();
    }

    public void show(){
        view.setVisibility(View.VISIBLE);//刷新时必现
        flag = true;
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(background, "alpha", 0, 1);
        fadeIn.setDuration(ANIMATION_DURATION);
        fadeIn.start();

        ObjectAnimator slideUp = ObjectAnimator.ofFloat(background, "y", getActivity().findViewById(android.R.id.content).getMeasuredHeight(),
                0);
            slideUp.setDuration(ANIMATION_DURATION);
            slideUp.start();
        slideUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
    }


    @OnClick({R2.id.map_bg, R2.id.map_bottom_bar,R2.id.addToPlan,R2.id.useGuide,R2.id.layout_tasks_not_pick,R2.id.layout_tasks_picked,R2.id.marketRoutePlan})
    public void OnClick(View view) {
        if (view.getId() == R.id.map_bg) {
            if (flag) {
                dismiss();
                background.setClickable(false);
            }
        } else if (view.getId() == R.id.map_bottom_bar) {
            getActivity().finish();
        } else if (view.getId() == R.id.addToPlan) {
            if(latLng != null) {
                if (marketRoutePlanListener != null) {
                    if (addOrRemovePlan == 0) {//加入规划
                        if (routePlanList.size() < 5) {
                            marketRoutePlanListener.onAddMarketToPlan(naviMarketId, latLng);
                        }else {
                            toast("不能再加了，最多支持5个门店");
                            return;
                        }
                    }else {
                        marketRoutePlanListener.onRemoveMarketToPlan(naviMarketId,latLng);
                    }
                    dismiss();
                    background.setClickable(false);
                }
            }
        } else if (view.getId() == R.id.useGuide) {
            if(latLng != null && StringUtil.isNotBlank(naviAddress)){
                openLocalMap(latLng.latitude,latLng.longitude,naviAddress);
            }
        } else if (view.getId() == R.id.layout_tasks_not_pick) {
            if(currentSelectIndex == 0){
                return;
            }else {
                currentSelectIndex = 0;
                textNotPick.setTextColor(getActivity().getResources().getColor(R.color.color_base));
                lineNotPick.setBackgroundColor(getActivity().getResources().getColor(R.color.color_base));
                textPicked.setTextColor(getActivity().getResources().getColor(R.color.text_color6));
                linePicked.setBackgroundColor(getActivity().getResources().getColor(R.color.text_color6));
                taskListAdapter.updateTasks(notPickTaskList);
            }
        } else if (view.getId() == R.id.layout_tasks_picked) {
            if(currentSelectIndex == 1){
                return;
            }else {
                currentSelectIndex = 1;
                textPicked.setTextColor(getActivity().getResources().getColor(R.color.color_base));
                linePicked.setBackgroundColor(getActivity().getResources().getColor(R.color.color_base));
                textNotPick.setTextColor(getActivity().getResources().getColor(R.color.text_color6));
                lineNotPick.setBackgroundColor(getActivity().getResources().getColor(R.color.text_color6));
                taskListAdapter.updateTasks(myTaskList);
            }
        } else if (view.getId() == R.id.marketRoutePlan) {
            if(latLng != null) {
                if (marketRoutePlanListener != null) {
                    dismiss();
                    background.setClickable(false);
                    marketRoutePlanListener.onMarketRoutePlan(latLng);
                }
            }
        }
    }


    /**
     * 显示或则关闭任务列表
     */
    public void showOrDismiss(String marketId, LatLng latLng, String address, String marketName) {
        view.setVisibility(View.VISIBLE);
        if (flag) {
            dismiss();
            background.setClickable(false);
        } else {
            show();
            background.setClickable(true);
        }
        if(latLng != null){//点击地图门店进来的
            mapBottomBar.setVisibility(View.GONE);
            mapGuideLayout.setVisibility(View.VISIBLE);
            mapMarketLayout.setVisibility(View.VISIBLE);
            //tasksStatusLayout.setVisibility(View.VISIBLE);
            tvMapResult.setText(marketName + "(" + nearNunber + "条任务)");
            tvMarketAddress.setText(address);
            this.latLng = latLng;
            this.naviAddress = address;
            this.naviMarketId = marketId;
            boolean ifExist = false;
            for (int i = 0; i < routePlanList.size(); i++) {
                LatLng llg = routePlanList.get(i);
                if(llg.longitude == latLng.longitude || llg.longitude == latLng.longitude) {
                    ifExist = true;
                }
            }
            if (ifExist) {//已经加过路线规划了
                addToPlanText.setText("移出规划");
                addOrRemovePlan = 1;
            }else {
                addToPlanText.setText("加入规划");
                addOrRemovePlan = 0;
            }
            if (onRoutePlan) {
                addToPlanText.setTextColor(getActivity().getResources().getColor(R.color.enable_color));
                addToPlanText.setEnabled(false);
            }else {
                if (ifExist) {//已经加过路线规划了
                    addToPlanText.setTextColor(getActivity().getResources().getColor(R.color.enable_color));
                }else {
                    addToPlanText.setTextColor(getActivity().getResources().getColor(R.color.color_base));
                }
                addToPlanText.setEnabled(true);
            }
        }else {
            mapBottomBar.setVisibility(View.VISIBLE);
            mapGuideLayout.setVisibility(View.GONE);
            mapMarketLayout.setVisibility(View.GONE);
            //tasksStatusLayout.setVisibility(View.GONE);
        }
        currentSelectIndex = 0;
        textNotPick.setTextColor(getActivity().getResources().getColor(R.color.color_base));
        lineNotPick.setBackgroundColor(getActivity().getResources().getColor(R.color.color_base));
        textPicked.setTextColor(getActivity().getResources().getColor(R.color.text_color6));
        linePicked.setBackgroundColor(getActivity().getResources().getColor(R.color.text_color6));
    }


    /**
     * 刷新列表中的数据
     */
    public void updateMapTasks (List<Task> taskList) {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
        nearNunber = 0;
        nearNunber = taskList.size();
        myTaskList.clear();
        notPickTaskList.clear();
        for(Task task:taskList){
            if(task.getStatus().equals("2") || task.getStatus().equals("3")){
                myTaskList.add(task);
            }else {
                notPickTaskList.add(task);
            }
        }
        tvMapResult.setText(SliceApp.CONTEXT.getString(R.string.map_search_result, nearNunber));
        taskListAdapter.updateTasks(notPickTaskList);
    }

    /**
     * 刷新已经添加到路线规划的门店
     */
    public void updateRoutePlanMarkets (List<LatLng> routePlanList) {
        this.routePlanList.clear();
        this.routePlanList.addAll(routePlanList);
    }

    /**
     * 添加列表数据
     * @param taskList
     */
    public void addMapTasks (List<Task> taskList) {
        nearNunber = nearNunber + taskList.size();
        tvMapResult.setText(SliceApp.CONTEXT.getString(R.string.map_search_result, nearNunber));
        taskListAdapter.addTasks(taskList, TaskListAdapter.SOURCE_MAP_TASK);
    }


    @Subscribe
    public void onTaskStatusChangedEvent(AppEvent.TaskStatusEvent event) {
        taskListAdapter.updateTaskStatus(event.taskid, event.status);
    }

    /**
     * 多人单抢到时用生成的工单替换列表中的多人单
     * @param event
     */
    @Subscribe
    public void onReplaceTaskEvent(AppEvent.ReplaceTaskEvent event) {
        taskListAdapter.updateTask(event.oldTaskId, event.newTask);
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {//重写这个方法，
        super.onSaveInstanceState(outState);
    }


    public interface MapChangeEventInterFace {

        public void mapMaxLayout();

        public void mapMinLayout();

        public void refreshTask(int start);
    }

    /**
     * 多人单取消时刷新列表
     * @param event
     */
    @Subscribe
    public void onRefreshTaskEvent(AppEvent.RefreshTaskEvent event) {
        if (StringUtil.isNotBlank(event.status) && event.status.equals("map_task_list")) {
            Log.d("-----------------", "收到消息，刷新地图任务");
            taskListAdapter.notifyDataSetChanged();
        }
    }

    /**
     *
     * @param dlat
     * @param dlon
     * @param address 当前位置
     */
    private void openLocalMap(double dlat, double dlon, String address) {
        new ActionSheetDialog(getActivity())
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("打开百度地图", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openBaiduMap(dlat,dlon,address);
                            }
                        })
                .addSheetItem("打开高德地图", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openGaodeNavigation(dlat,dlon,address);
                            }
                        })
                .addSheetItem("评价导航", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                showNaviReviewDialog();
                            }
                        }).show();

    }

    /**
     *  打开百度地图
     */
    private void openBaiduMap(double dlat, double dlon, String dname) {
        if(OpenLocalMapUtil.isBaiduMapInstalled()){
            try {
                String uri = OpenLocalMapUtil.getBaiduMapUri(dname, SRC);
                Intent intent = Intent.parseUri(uri, 0);
                startActivityForResult(intent,REQUEST_CODE_MAP_NAVI); //启动调用

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            toast("您尚未安装百度地图，请安装百度地图后使用此功能");
        }
    }

    private void openGaodeNavigation(double dlat, double dlon, String dname) {//打开高德导航
        if (OpenLocalMapUtil.isGdMapInstalled()) {
            try {
                //double[] gaodeGps = OpenLocalMapUtil.bdToGaoDe(dlat, dlon);
                String uri = OpenLocalMapUtil.getGdMapUri("", dname);
                Intent intent = Intent.parseUri(uri, 0);
                startActivityForResult(intent,REQUEST_CODE_MAP_NAVI); //启动调用
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            toast("您尚未安装高德地图，请安装高德地图后使用此功能");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_MAP_NAVI){
            boolean ifOpenNaviReview = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getBoolean("NOT_OPEN_NAVI_REVIEW_ANYMORE", false);
            if(!ifOpenNaviReview){
                showNaviReviewDialog();
            }
        }
    }

    public void showNaviReviewDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View view = layoutInflater.inflate(R.layout.dialog_review_navi, null);
        ScaleRatingBar ratingBar = (ScaleRatingBar) view.findViewById(R.id.simpleRatingBar);
        LinearLayout advice_layout = (LinearLayout) view.findViewById(R.id.advice_layout);
        LinearLayout one_two_star_layout = (LinearLayout) view.findViewById(R.id.one_two_star_layout);
        LinearLayout three_four_star_layout = (LinearLayout) view.findViewById(R.id.three_four_star_layout);
        TextView open_map_fail = (TextView) view.findViewById(R.id.open_map_fail);
        TextView navi_error = (TextView) view.findViewById(R.id.navi_error);
        TextView address_not_exist = (TextView) view.findViewById(R.id.address_not_exist);
        TextView other = (TextView) view.findViewById(R.id.other);
        EditText edit_other_advice = (EditText) view.findViewById(R.id.edit_other_advice);
        TextView address_name_not_match = (TextView) view.findViewById(R.id.address_name_not_match);
        TextView open_map_soft_fail = (TextView) view.findViewById(R.id.open_map_soft_fail);
        TextView have_no_map_soft = (TextView) view.findViewById(R.id.have_no_map_soft);
        TextView three_other = (TextView) view.findViewById(R.id.three_other);
        Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
        Button btDefine = (Button) view.findViewById(R.id.dialog_define);
        EditText three_edit_other_advice = (EditText) view.findViewById(R.id.three_edit_other_advice);
        CheckBox not_open_anymore = (CheckBox) view.findViewById(R.id.not_open_anymore);
        LinearLayout notOpenAnyMoreLayout = (LinearLayout) view.findViewById(R.id.not_open_anymore_layout);
        ArrayList<String> oneTwoStarAdvices = new ArrayList<>();
        ArrayList<String> threeFourStarAdvices = new ArrayList<>();

        ratingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float ratingCount) {
                if(ratingCount == 0 || ratingCount == 5.0){
                    advice_layout.setVisibility(View.GONE);
                }else if(ratingCount == 1.0 || ratingCount == 2.0){
                    advice_layout.setVisibility(View.VISIBLE);
                    one_two_star_layout.setVisibility(View.VISIBLE);
                    three_four_star_layout.setVisibility(View.GONE);
                    open_map_fail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(oneTwoStarAdvices.contains(NAVI_REVIEW_COMMENT1)){
                                oneTwoStarAdvices.remove(NAVI_REVIEW_COMMENT1);
                                open_map_fail.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                open_map_fail.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                oneTwoStarAdvices.add(NAVI_REVIEW_COMMENT1);
                            }
                        }
                    });
                    navi_error.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(oneTwoStarAdvices.contains(NAVI_REVIEW_COMMENT2)){
                                oneTwoStarAdvices.remove(NAVI_REVIEW_COMMENT2);
                                navi_error.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                navi_error.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                oneTwoStarAdvices.add(NAVI_REVIEW_COMMENT2);
                            }
                        }
                    });
                    address_not_exist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(oneTwoStarAdvices.contains(NAVI_REVIEW_COMMENT3)){
                                oneTwoStarAdvices.remove(NAVI_REVIEW_COMMENT3);
                                address_not_exist.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                address_not_exist.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                oneTwoStarAdvices.add(NAVI_REVIEW_COMMENT3);
                            }
                        }
                    });
                    other.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(oneTwoStarAdvices.contains(NAVI_REVIEW_COMMENT7)){
                                oneTwoStarAdvices.remove(NAVI_REVIEW_COMMENT7);
                                other.setBackgroundResource(R.drawable.shape_normal_white_corners);
                                edit_other_advice.setText("");
                                edit_other_advice.setVisibility(View.GONE);
                            }else {
                                other.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                oneTwoStarAdvices.add(NAVI_REVIEW_COMMENT7);
                                edit_other_advice.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else if(ratingCount == 3.0 || ratingCount == 4.0){
                    advice_layout.setVisibility(View.VISIBLE);
                    one_two_star_layout.setVisibility(View.GONE);
                    three_four_star_layout.setVisibility(View.VISIBLE);
                    address_name_not_match.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(threeFourStarAdvices.contains(NAVI_REVIEW_COMMENT4)){
                                threeFourStarAdvices.remove(NAVI_REVIEW_COMMENT4);
                                address_name_not_match.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                address_name_not_match.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                threeFourStarAdvices.add(NAVI_REVIEW_COMMENT4);
                            }
                        }
                    });
                    open_map_soft_fail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(threeFourStarAdvices.contains(NAVI_REVIEW_COMMENT5)){
                                threeFourStarAdvices.remove(NAVI_REVIEW_COMMENT5);
                                open_map_soft_fail.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                open_map_soft_fail.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                threeFourStarAdvices.add(NAVI_REVIEW_COMMENT5);
                            }
                        }
                    });
                    have_no_map_soft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(threeFourStarAdvices.contains(NAVI_REVIEW_COMMENT6)){
                                threeFourStarAdvices.remove(NAVI_REVIEW_COMMENT6);
                                have_no_map_soft.setBackgroundResource(R.drawable.shape_normal_white_corners);
                            }else {
                                have_no_map_soft.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                threeFourStarAdvices.add(NAVI_REVIEW_COMMENT6);
                            }
                        }
                    });
                    three_other.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(threeFourStarAdvices.contains(NAVI_REVIEW_COMMENT7)){
                                threeFourStarAdvices.remove(NAVI_REVIEW_COMMENT7);
                                three_other.setBackgroundResource(R.drawable.shape_normal_white_corners);
                                three_edit_other_advice.setText("");
                                three_edit_other_advice.setVisibility(View.GONE);
                            }else {
                                three_other.setBackgroundResource(R.drawable.shape_text_bg_selected);
                                threeFourStarAdvices.add(NAVI_REVIEW_COMMENT7);
                                three_edit_other_advice.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else {
                    advice_layout.setVisibility(View.GONE);
                }
            }
        });
        not_open_anymore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtil.make(getActivity(), PrefUtil.PREFERENCE_NAME).putBoolean("NOT_OPEN_NAVI_REVIEW_ANYMORE", isChecked);
            }
        });
        notOpenAnyMoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                not_open_anymore.setChecked(!not_open_anymore.isChecked());
            }
        });
        builder.setCancelable(false);
        naviReviewDialog = builder.create();
        naviReviewDialog.show();
        btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
            naviReviewDialog.dismiss();
        });
        btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
            //naviReviewDialog.dismiss();
            int score = (int) ratingBar.getRating();
            if(score == 0){
                toast("请对此次导航做出评价");
                return;
            }
            StringBuilder commentContent = new StringBuilder();
            if(score == 1 || score == 2){
                if(oneTwoStarAdvices.size() == 1) {
                    commentContent.append(oneTwoStarAdvices.get(0));
                }else if (oneTwoStarAdvices.size() > 1){
                    for (int i=0;i<oneTwoStarAdvices.size();i++) {
                        if(i != oneTwoStarAdvices.size() - 1){
                            commentContent.append(oneTwoStarAdvices.get(i) + ",");
                        }else{
                            commentContent.append(oneTwoStarAdvices.get(i));
                        }
                    }
                }
                if(StringUtil.isNotBlank(edit_other_advice.getText().toString())){
                    if(commentContent.toString().contains("其他")){
                        commentContent.insert(commentContent.toString().indexOf("其他") + 2,":" + edit_other_advice.getText().toString());
                    }
                }
            } else if(score == 3 || score == 4){
                if(threeFourStarAdvices.size() == 1) {
                    commentContent.append(threeFourStarAdvices.get(0));
                }else if (threeFourStarAdvices.size() > 1){
                    for (int i=0;i<threeFourStarAdvices.size();i++) {
                        if(i != threeFourStarAdvices.size() - 1){
                            commentContent.append(threeFourStarAdvices.get(i) + ",");
                        }else{
                            commentContent.append(threeFourStarAdvices.get(i));
                        }
                    }
                }
                if(StringUtil.isNotBlank(three_edit_other_advice.getText().toString())){
                    if(commentContent.toString().contains("其他")){
                        commentContent.insert(commentContent.toString().indexOf("其他") + 2,":" + three_edit_other_advice.getText().toString());
                    }
                }
            }
            if(score < 5 && StringUtil.isBlank(commentContent.toString())){
                toast("请对此次导航做出评价");
                return;
            }
            commitNaviReview(score,commentContent.toString());
        });
        Window dialogWindow = naviReviewDialog.getWindow();
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager m = getActivity().getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    private void commitNaviReview(int score,String comment){
        showProgressDialog();
        String timestamp = DateUtil.getCurrentTime();
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        SignUtil.SignBuilder signBuilder = new SignUtil.SignBuilder();
        User user = BizLogic.getCurrentUser();
        signBuilder.put("userid", user.userid)
                .put("marketid", naviMarketId)
                .put("score", score+"")
                .put("comment", comment)
                .put("appId", appId);
        String sig = signBuilder.build();
        Api api = RestClient.getInstance().provideApi();
        Observable<Response<Object>> naviReviewObservable = null;
        naviReviewObservable = api.commitMapComments(user.userid, naviMarketId, score+"",comment, appId,sig);
        naviReviewObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    dismissProgressDialog();
                    if (res.ret == 0) {
                        toast("导航评价成功");
                        if(naviReviewDialog != null){
                            naviReviewDialog.dismiss();
                        }
                    } else {
                        toast(res.msg);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        toast("提交失败，请检查当前网络环境是否正常");
                        dismissProgressDialog();
                    }
                });

    }

    class MyItemDecoration extends RecyclerView.ItemDecoration {
        /**
         *
         * @param outRect 边界
         * @param view recyclerView ItemView
         * @param parent recyclerView
         * @param state recycler 内部数据管理
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //设定底部边距为1px
            outRect.set(0, 0, 0, 2);
        }
    }

    public interface MarketRoutePlanListener{
        void onMarketRoutePlan(LatLng marketLatlng);
        void onAddMarketToPlan(String marketId, LatLng marketLatlng);
        void onRemoveMarketToPlan(String marketId, LatLng marketLatlng);
    }
}
