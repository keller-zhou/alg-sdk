package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.listener.MyOrientationListener;
import com.slicejobs.algsdk.algtasklibrary.model.MapMarkerOverlay;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.TaskPresenter;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.CardPagerAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.TaskListAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.MapTaskListFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.RadarLayout;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ShadowTransformer;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.overlayutil.DrivingRouteOverlay;
import com.slicejobs.algsdk.algtasklibrary.utils.overlayutil.TransitRouteOverlay;
import com.slicejobs.algsdk.algtasklibrary.utils.overlayutil.WalkingRouteOverlay;
import com.slicejobs.algsdk.algtasklibrary.view.ITaskView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by keller.zhou on 16/3/31.
 */
public class MapActivity extends BaseActivity implements MapTaskListFragment.MapChangeEventInterFace, ITaskView,OnGetGeoCoderResultListener,MapTaskListFragment.MarketRoutePlanListener {
    private static final int ROUTE_PLAN_DIRVING = 0;
    private static final int ROUTE_PLAN_WALKING = 1;
    private static final int ROUTE_PLAN_BUS = 2;
    public static final String MY_LOCATION = "mylocation";
    public static final String LON = "lon";
    public static final String LAT = "lat";
    public static final String CLICKMARKETID = "clickMarketId";
    public static final String CLICKMARKETLON = "clickMarketLon";
    public static final String CLICKMARKETLAT = "clickMarketLat";
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private MapTaskListFragment mapTaskListFragment = null;

    @BindView(R2.id.layout_bmapview)
    FrameLayout layoutBmapView;
    @BindView(R2.id.tv_sum_money)
    TextView tvSumMoney;
    @BindView(R2.id.iv_my_location_pas)
    ImageView ivMyLocationPas;
    @BindView(R2.id.my_location_radar)
    RadarLayout myLocationRadar;
    @BindView(R2.id.layout_map_hint)
    LinearLayout layoutMapHint;
    @BindView(R2.id.tv_map_hint)
    TextView tvMapHint;
    @BindView(R2.id.route_plan_layout)
    LinearLayout routePlanLayout;
    @BindView(R2.id.title_map)
    TextView mapTitle;
    @BindView(R2.id.route_plan_drive_text)
    ImageView routePlanDrive;
    @BindView(R2.id.route_plan_bus_text)
    ImageView routePlanBus;
    @BindView(R2.id.route_plan_walk_text)
    ImageView routePlanWalk;
    @BindView(R2.id.route_plan_drive_min)
    TextView routePlanDriveMin;
    @BindView(R2.id.route_plan_bus_min)
    TextView routePlanBusMin;
    @BindView(R2.id.route_plan_walk_min)
    TextView routePlanWalkMin;
    @BindView(R2.id.viewPager)
    ViewPager routePlanBusVp;
    @BindView(R2.id.add_market_frame_notice)
    FrameLayout addMarketHelpFrame;
    @BindView(R2.id.multi_route_plan_frame_notice)
    FrameLayout routePlanHelpFrame;
    @BindView(R2.id.multi_route_plan)
    LinearLayout multiRoutePlan;
    @BindView(R2.id.multi_route_plan_review)
    FrameLayout multiRoutePlanReview;
    @BindView(R2.id.layout_show_map_list)
    FrameLayout allTaskLayout;
    @BindView(R2.id.route_plan_bus)
    LinearLayout rpBusLayout;
    @BindView(R2.id.route_plan_walk)
    LinearLayout rpWalkLayout;
    @BindView(R2.id.tasks_search_layout)
    LinearLayout taskSearchLayout;
    @BindView(R2.id.et_search_task)
    EditText searchTask;
    @BindView(R2.id.cacel_layout)
    FrameLayout cacelLayout;
    @BindView(R2.id.map_task_list)//用于显示搜索出来的任务
            RecyclerView rvNearTaskList;
    @BindView(R2.id.search_no_data)
    FrameLayout frameNoData;
    @BindView(R2.id.route_plan_quit)
    LinearLayout routePlanQuitLayout;
    TaskListAdapter taskListAdapter;
    @BindView(R2.id.layout_route_distance)
    FrameLayout routeDiatanceLayout;
    @BindView(R2.id.text_route_distance)
    TextView routeDiatanceText;
    private List<Task> taskListSum = new ArrayList<>();//当前总列表
    private double lon;
    private double lat;

    private TaskPresenter presenter;
    private int startId = 0;
    private boolean isLoadingMore = false;
    private boolean loadAll = false;

    private Map<String, MapMarkerOverlay> map = new HashMap<>();//门店任务将金额加起来汇总

    private LocationClient locationClient;

    private List<Task> clickList = new ArrayList<Task>();//点击门店，显示任务
    private List<LatLng> routePlanList = new ArrayList<LatLng>();
    private float mapNumMoney = 0;//总金额

    private String distance = "15";//默认获取地图5公里距离

    /**定位覆盖物*/
    private MarkerOptions myOptions;//自身位置图标(或则移动位置的图标)
    private Marker myMarker;
    private BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);

    /**超市覆盖物*/
    private List<Marker> listMarkers = new ArrayList<>();
    private String clickTaskMarketId;
    private float mCurrentX;
    //自定义图标
    private BitmapDescriptor mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
    private MyOrientationListener myOrientationListener;
    /**
     * 当前的精度
     */
    private float mCurrentAccracy;
    private double mCurrentLantitude,mCurrentLongitude;
    private boolean ifFirstLoadData = true;
    private AddMarketPointTask addMarketPointTask;
    private float zoom = 15;
    private LatLng startLatlng;
    private LatLng endLatlng;
    private int currentRoutePlan;
    private BDLocation currentBDLocation;
    private WalkingRouteOverlay walkingRouteOverlay;
    private DrivingRouteOverlay drivingRouteOverlay;
    private TransitRouteOverlay transitRouteOverlay;
    private RoutePlanSearch mRPSearch;
    private CardPagerAdapter cardPagerAdapter;
    private ShadowTransformer shadowTransformer;
    private boolean ifFirstPlanRoute = true;
    private boolean ifMultiPlan;
    private boolean isPlanRoute;
    private boolean isRefreshMyLoaction;

    public static Intent getMapActivityIntent(Context context, double lon, double lat) {//首次把位置传入
        Intent intent = new Intent(context, MapActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra(LON, lon);
        intent.putExtra(LAT, lat);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent getMapActivityIntent(Context context, double lon, double lat, String clickMarketId, String clickMarketLon, String clickMarketLat) {//任务详情点击的门店id
        Intent intent = new Intent(context, MapActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra(LON, lon);
        intent.putExtra(LAT, lat);
        intent.putExtra(CLICKMARKETID, clickMarketId);
        intent.putExtra(CLICKMARKETLON, clickMarketLon);
        intent.putExtra(CLICKMARKETLAT, clickMarketLat);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        presenter = new TaskPresenter(this);

        initData();

        if (mapTaskListFragment == null) {
            mapTaskListFragment = MapTaskListFragment.newInstance(this,this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_maptask, mapTaskListFragment)
                    .commit();
        }


        //删除提示
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.task_loader_msg);
        anim.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                layoutMapHint.setVisibility(View.INVISIBLE);
            }
        });
        layoutMapHint.setAnimation(anim);
        myOrientationListener=new MyOrientationListener(this);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX=x;
                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentX)
                        .latitude(mCurrentLantitude)
                        .longitude(mCurrentLongitude).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                MyLocationConfiguration config = new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
                mBaiduMap.setMyLocationConfigeration(config);
            }
        });

        searchTask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = searchTask.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null) {
                    searchTask.requestFocus();
                    cacelLayout.setVisibility(View.VISIBLE);
                    return false;
                }
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > searchTask.getWidth()
                        - searchTask.getPaddingRight()
                        - drawable.getIntrinsicWidth()){
                    searchTask.setText("");
                    taskListAdapter.clearTasks();
                    frameNoData.setVisibility(View.GONE);
                }
                return false;
            }
        });

        //监听输入框
        searchTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Resources res = getResources();
                Drawable close = res.getDrawable(R.drawable.ic_search_close);
                close.setBounds(0, 0, close.getMinimumWidth(), close.getMinimumHeight());
                if(StringUtil.isBlank(searchTask.getText().toString())) {
                    searchTask.setCompoundDrawables(null,null,null,null);
                }else{
                    searchTask.setCompoundDrawables(null,null,close,null);
                }
                keywordSearchTask(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        taskListAdapter = new TaskListAdapter(TaskListAdapter.TYPE_MAP);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvNearTaskList.setLayoutManager(mLayoutManager);
        rvNearTaskList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvNearTaskList.setAdapter(taskListAdapter);
        taskListAdapter.setCallback(new TaskListAdapter.ItemClickCallback() {
            @Override
            public void onItemClick(View view, Task task, int position) {
                searchTask.setText("");
                taskListAdapter.clearTasks();
                searchTask.clearFocus();
                cacelLayout.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                lat = Double.parseDouble(task.getLatitude());
                lon = Double.parseDouble(task.getLongitude());
                //地图移动中心点
                LatLng cenpt = new LatLng(lat, lon);
                MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(zoom).build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                try {
                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                } catch (NullPointerException e) {
                    Log.d("slicejobs", "baidu accidental null pointer");
                }

                showUserMarket();
                if (presenter != null && lat != 39.914884096217335 && lon != 116.40388321804957) {
                    if(getMapRadius() != -1){
                        if(getMapRadius() >= 20){
                            distance = 20 + "";
                        }else {
                            distance = getMapRadius() + "";
                        }
                    }
                    startId = 0;
                    isLoadingMore = true;
                    loadAll = false;
                    handler.sendEmptyMessageDelayed(1,1500);
                    presenter.getNearbyTask(lat + "", lon + "", mCurrentLongitude + "", mCurrentLantitude + "",distance);
                }
            }
        });
    }

    public void keywordSearchTask(String keyWord) {
        if (StringUtil.isNotBlank(keyWord)) {
            if (presenter != null && lat != 39.914884096217335 && lon != 116.40388321804957) {
                presenter.getNearbyTaskByKeyword(lat + "", lon + "", "20", keyWord);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void initData() {//获得定位，向附近获取5公里数据
        Intent intent = getIntent();
        clickTaskMarketId = intent.getStringExtra(CLICKMARKETID);
        if(StringUtil.isNotBlank(clickTaskMarketId) && StringUtil.isNotBlank(intent.getStringExtra(CLICKMARKETLON)) && StringUtil.isNotBlank(intent.getStringExtra(CLICKMARKETLAT))){//首页进来的
            lon = Double.parseDouble(intent.getStringExtra(CLICKMARKETLON));
            lat = Double.parseDouble(intent.getStringExtra(CLICKMARKETLAT));
            endLatlng = new LatLng(lat,lon);
            zoom = 18;
        }else {
            lon = intent.getDoubleExtra(LON, 0.0);
            lat = intent.getDoubleExtra(LAT, 0.0);
            //首页进来的显示5公里范围
            /*
             * 20米,50米，100米，200米，500米，1公里，2公里，5公里，10公里，20公里，25公里，50公里，100公里，200公里，500公里，1000公里，2000公里，5000公里，10000公里]
             * 分别对应：
             * [19级，18级，17级，16级，15级，14级，13级，12级，11级，10级，9级，8级，7级，6级，5级，4级，3级，2级，1级]
             * */
            zoom = 15;
        }
        changeMapZoom(zoom);
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //地图拖动监听
        mBaiduMap.setOnMapStatusChangeListener(new MyMapMoveListener());

        //覆盖物点击监听
        setFlagImageClick();

        getLocationTask();
    }


    private void startLocating() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setScanSpan(2000);

        //option.addrType = "all";
        //option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClient = new LocationClient(this);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(bdAbstractLocationListener);
        locationClient.start();
    }

    private BDAbstractLocationListener bdAbstractLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && bdLocation.getLongitude() != 4.9E-324 && bdLocation.getLatitude() != 4.9E-324 && bdLocation.getLongitude() != 0 && bdLocation.getLatitude() != 0) {
                mCurrentLantitude = bdLocation.getLatitude();
                mCurrentLongitude = bdLocation.getLongitude();
                currentBDLocation = bdLocation;
                //mCurrentAccracy = bdLocation.getRadius();
                MyLocationData data= new MyLocationData.Builder()
                        .direction(mCurrentX)//设定图标方向
                        .accuracy(mCurrentAccracy)//getRadius 获取定位精度,默认值0.0f
                        .latitude(mCurrentLantitude)//百度纬度坐标
                        .longitude(mCurrentLongitude)//百度经度坐标
                        .build();
                //设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
                mBaiduMap.setMyLocationData(data);
                //配置定位图层显示方式,三个参数的构造器
                /*
                 * 1.定位图层显示模式
                 * 2.是否允许显示方向信息
                 * 3.用户自定义定位图标
                 *
                 * */
                MyLocationConfiguration configuration
                        =new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,mIconLocation);
                //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
                mBaiduMap.setMyLocationConfigeration(configuration);
                if(ifFirstLoadData) {
                    startLatlng = new LatLng(mCurrentLantitude,mCurrentLongitude);
                    if (startLatlng != null && endLatlng != null) {
                        if (!isRefreshMyLoaction) {
                            if (endLatlng.latitude != 39.914884096217335 && endLatlng.longitude != 116.40388321804957) {
                                //地图移动中心点
                                MapStatus mMapStatus = new MapStatus.Builder().target(endLatlng).zoom(zoom).build();
                                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                                try {
                                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                                } catch (NullPointerException e) {
                                    Log.d("slicejobs", "baidu accidental null pointer");
                                }

                                try {
                                    if (myMarker != null) {
                                        myMarker.remove();
                                        myMarker = null;
                                    }
                                } catch (Exception e) {
                                    Log.d("RimTaskMapActivity", "null");
                                }

                                //设置当前位置的地图坐标
                                myOptions = new MarkerOptions()
                                        .position(endLatlng)  //设置位置
                                        .icon(descriptor)  //设置图标
                                        .zIndex(9)  //设置r所在层级
                                        .title(MY_LOCATION)
                                        .draggable(false);  //设置手势拖拽
                                myOptions.anchor(0.5f, 1.0f);
                                myOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
                                myMarker = (Marker) mBaiduMap.addOverlay(myOptions);
                                myMarker.setToTop();
                            }

                            isPlanRoute = true;
                            double distance = DistanceUtil.getDistance(startLatlng, endLatlng);
                            if (distance < 20 * 1000) {//起点和终点距离在20公里内才路线规划
                                mapTitle.setVisibility(View.GONE);
                                routePlanLayout.setVisibility(View.VISIBLE);
                                myLocationRadar.setVisibility(View.GONE);
                                routePlanQuitLayout.setVisibility(View.INVISIBLE);
                                planRouteToMarket(ROUTE_PLAN_BUS);
                            } else {
                                showHintDialog(new DialogDefineClick() {
                                    @Override
                                    public void defineClick() {

                                    }
                                }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), "当前门店距离过远，暂不支持路线规划", "我知道了", true);
                            }
                        } else {
                            if (startLatlng.latitude != 39.914884096217335 && startLatlng.longitude != 116.40388321804957) {
                                //地图移动中心点
                                MapStatus mMapStatus = new MapStatus.Builder().target(startLatlng).zoom(zoom).build();
                                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                                try {
                                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                                } catch (NullPointerException e) {
                                    Log.d("slicejobs", "baidu accidental null pointer");
                                }

                                try {
                                    if (myMarker != null) {
                                        myMarker.remove();
                                        myMarker = null;
                                    }
                                } catch (Exception e) {
                                    Log.d("RimTaskMapActivity", "null");
                                }

                                //设置当前位置的地图坐标
                                myOptions = new MarkerOptions()
                                        .position(startLatlng)  //设置位置
                                        .icon(descriptor)  //设置图标
                                        .zIndex(9)  //设置r所在层级
                                        .title(MY_LOCATION)
                                        .draggable(false);  //设置手势拖拽
                                myOptions.anchor(0.5f, 1.0f);
                                myOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
                                myMarker = (Marker) mBaiduMap.addOverlay(myOptions);
                                myMarker.setToTop();
                            }
                        }
                        taskSearchLayout.setVisibility(View.GONE);
                        allTaskLayout.setVisibility(View.GONE);
                        ifFirstLoadData = false;
                    } else {
                        if (lon == 0.0 || lat == 0.0) {
                            lat = mCurrentLantitude;
                            lon = mCurrentLongitude;
                        }

                        if (lat != 39.914884096217335 && lon != 116.40388321804957) {
                            //地图移动中心点
                            LatLng cenpt = new LatLng(lat, lon);
                            MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(zoom).build();
                            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                            try {
                                mBaiduMap.setMapStatus(mMapStatusUpdate);
                            } catch (NullPointerException e) {
                                Log.d("slicejobs", "baidu accidental null pointer");
                            }

                            try {
                                if (myMarker != null) {
                                    myMarker.remove();
                                    myMarker = null;
                                }
                            } catch (Exception e) {
                                Log.d("RimTaskMapActivity", "null");
                            }

                            //设置当前位置的地图坐标
                            myOptions = new MarkerOptions()
                                    .position(cenpt)  //设置位置
                                    .icon(descriptor)  //设置图标
                                    .zIndex(9)  //设置r所在层级
                                    .title(MY_LOCATION)
                                    .draggable(false);  //设置手势拖拽
                            myOptions.anchor(0.5f, 1.0f);
                            myOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
                            myMarker = (Marker) mBaiduMap.addOverlay(myOptions);
                            myMarker.setToTop();
                            myLocationRadar.start();
                            if (presenter != null) {
                                if (getMapRadius() != -1) {
                                    if (getMapRadius() >= 20) {
                                        distance = 20 + "";
                                    } else {
                                        distance = getMapRadius() + "";
                                    }
                                }
                                presenter.getNearbyTask(lat + "", lon + "", mCurrentLongitude + "", mCurrentLantitude + "", distance);
                            }
                            //locationClient.unRegisterLocationListener(this);
                            ifFirstLoadData = false;
                        } else {
                            startId = 0;
                            isLoadingMore = true;
                            loadAll = false;
                            ifFirstLoadData = true;
                            lat = 0.0;
                            lon = 0.0;
                            zoom = 15;
                            if (addMarketPointTask != null && !addMarketPointTask.isCancelled()) {
                                addMarketPointTask.cancel(true);
                                addMarketPointTask = null;
                                ivMyLocationPas.setVisibility(View.GONE);
                                System.gc();
                            }
                            getLocationTask();
                        }
                    }
                }
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        try {
            myOrientationListener.stop();
            if(locationClient != null){
                locationClient.stop();
                locationClient.unRegisterLocationListener(bdAbstractLocationListener);
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        myOrientationListener.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }


    @OnClick({R2.id.action_return, R2.id.layout_show_map_list,R2.id.iv_refresh_mylocation, R2.id.map_zoom_in,R2.id.map_zoom_out,
            R2.id.route_plan_drive,R2.id.route_plan_bus,R2.id.route_plan_walk,R2.id.route_plan_quit,R2.id.add_market_frame_notice,R2.id.multi_route_plan,
            R2.id.multi_route_plan_frame_notice,R2.id.multi_route_plan_review,R2.id.cacel_layout})
    public void OnClick(View view) {
        if (view.getId() == R.id.action_return) {
            MapActivity.this.finish();
        } else if (view.getId() == R.id.layout_show_map_list) {
            if (mapTaskListFragment != null) {
                mapTaskListFragment.updateMapTasks(taskListSum);
                mapTaskListFragment.showOrDismiss(null,null,null,null);
            }
        } else if (view.getId() == R.id.iv_refresh_mylocation) {
            startId = 0;
            isLoadingMore = true;
            loadAll = false;
            ifFirstLoadData = true;
            lat = 0.0;
            lon = 0.0;
            if(addMarketPointTask != null && !addMarketPointTask.isCancelled()) {
                addMarketPointTask.cancel(true);
                addMarketPointTask = null;
                ivMyLocationPas.setVisibility(View.GONE);
                System.gc();
            }
            isRefreshMyLoaction = true;
            getLocationTask();
        } else if (view.getId() == R.id.map_zoom_in) {
            if(zoom <= 20) {
                zoom = zoom + 1;
                changeMapZoom(zoom);
            }
        } else if (view.getId() == R.id.map_zoom_out) {
            if(zoom >= 2) {
                zoom = zoom - 1;
                changeMapZoom(zoom);
            }
        } else if (view.getId() == R.id.route_plan_drive) {
            if(currentRoutePlan == ROUTE_PLAN_DIRVING){
                return;
            }
            routePlanBusVp.setVisibility(View.GONE);
            planRouteToMarket(ROUTE_PLAN_DIRVING);
        } else if (view.getId() == R.id.route_plan_bus) {
            if(currentRoutePlan == ROUTE_PLAN_BUS){
                return;
            }
            if (ifMultiPlan) {
                toast("多门店导航目前仅支持驾车");
                return;
            }
            routePlanBusVp.setVisibility(View.VISIBLE);
            planRouteToMarket(ROUTE_PLAN_BUS);
        } else if (view.getId() == R.id.route_plan_walk) {
            if(currentRoutePlan == ROUTE_PLAN_WALKING){
                return;
            }
            if (ifMultiPlan) {
                toast("多门店导航目前仅支持驾车");
                return;
            }
            routePlanBusVp.setVisibility(View.GONE);
            planRouteToMarket(ROUTE_PLAN_WALKING);
        } else if (view.getId() == R.id.route_plan_quit) {
            mapTitle.setVisibility(View.VISIBLE);
            routePlanLayout.setVisibility(View.GONE);
            if(transitRouteOverlay != null) {
                transitRouteOverlay.removeFromMap();
            }
            if(drivingRouteOverlay != null) {
                drivingRouteOverlay.removeFromMap();
            }
            if(walkingRouteOverlay != null) {
                walkingRouteOverlay.removeFromMap();
            }
            routePlanBusVp.setVisibility(View.GONE);
            setMapCenterPoint();
            if (ifMultiPlan) {
                multiRoutePlanReview.setVisibility(View.GONE);
                allTaskLayout.setVisibility(View.VISIBLE);
                rpBusLayout.setVisibility(View.VISIBLE);
                rpWalkLayout.setVisibility(View.VISIBLE);
                ifMultiPlan = false;
            }
            mapTaskListFragment.setOnRoutePlan(false);
        } else if (view.getId() == R.id.add_market_frame_notice) {
            addMarketHelpFrame.setVisibility(View.GONE);
            PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putBoolean("IF_FIRST_USE_ROUTE_PLAN", false);
            if(!clickList.isEmpty()){
                Task task = clickList.get(0);
                String lastClickMarketId = task.getMarketid();
                MapMarkerOverlay lastMarkerOverlay = map.get(lastClickMarketId);
                routePlanList.add(new LatLng(lastMarkerOverlay.getLat(),lastMarkerOverlay.getLon()));
                MapMarkerOverlay mapMarkerOverlay = map.get(task.getMarketid());
                showMarketSelectedPoint(mapMarkerOverlay, task.getMarketid());
                multiRoutePlan.setVisibility(View.VISIBLE);
                routePlanHelpFrame.setVisibility(View.VISIBLE);
                if (mapTaskListFragment != null) {
                    mapTaskListFragment.dismiss();
                }
            }
        } else if (view.getId() == R.id.multi_route_plan) {
            multiMarketPlanRoute();
        } else if (view.getId() == R.id.multi_route_plan_frame_notice) {
            routePlanHelpFrame.setVisibility(View.GONE);
            multiMarketPlanRoute();
        } else if (view.getId() == R.id.multi_route_plan_review) {
            if (mapTaskListFragment != null) {
                mapTaskListFragment.showNaviReviewDialog();
            }
        } else if (view.getId() == R.id.cacel_layout) {
            searchTask.setText("");
            taskListAdapter.clearTasks();
            searchTask.clearFocus();
            cacelLayout.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            frameNoData.setVisibility(View.GONE);
        }
    }


    @Override
    public void mapMaxLayout() {

    }


    @Override
    public void mapMinLayout() {

    }


    /**
     * 显示门店覆盖物
     */
    public void showUser(MapMarkerOverlay mapMarkerOverlay, String marketId, boolean ifReDraw) {
        if (null != mapMarkerOverlay) {
            boolean ifAddMarker = true;
            TextView view = new TextView(this);
            int sum = (int) Math.round(mapMarkerOverlay.getSumMoney() - 0.5);
            if(sum >= 1) {
                if (StringUtil.isNotBlank(clickTaskMarketId) && clickTaskMarketId.equals(marketId)) {
                    view.setBackgroundResource(R.drawable.ic_markerpoint_bg_red);
                    view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                    view.setTextSize(9);
                    view.setGravity(Gravity.CENTER);
                    view.setPadding(0,0,0, DensityUtil.dip2px(this, 12));
                    if (sum <1000) {
                        view.setText(sum+"元");
                    } else {
                        view.setText((sum+"").substring(0,3)+"..\n元");
                    }
                } else {
                    if (mapMarkerOverlay.getMarketType() == 2) {
                        view.setBackgroundResource(R.drawable.ic_markerpoint_bg_flag);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_base));
                        view.setTextSize(8);
                        view.setPadding(DensityUtil.dip2px(this, 6),DensityUtil.dip2px(this, 8),0,0);
                        if (sum <100) {
                            view.setText(sum+"元");
                        } else {
                            view.setText((sum+"").substring(0,2)+"..元");
                        }
                    } else {
                        view.setBackgroundResource(R.drawable.ic_markerpoint_bg);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_base));
                        view.setTextSize(9);
                        view.setGravity(Gravity.CENTER);
                        view.setPadding(0,0,0,DensityUtil.dip2px(this, 12));
                        if (sum <1000) {
                            view.setText(sum+"元");
                        } else {
                            view.setText((sum+"").substring(0,3)+"..\n元");
                        }
                    }
                }
            }else {
                int pointSum = mapMarkerOverlay.getSumPoint();
                if(pointSum == 0){
                    ifAddMarker = false;
                }else {
                    if (StringUtil.isNotBlank(clickTaskMarketId) && clickTaskMarketId.equals(marketId)) {
                        view.setBackgroundResource(R.drawable.ic_task_point_red);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                    }else {
                        view.setBackgroundResource(R.drawable.ic_task_point);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_base));
                    }
                }
            }
            for (int i = 0; i < listMarkers.size(); i++) {
                Marker marker = listMarkers.get(i);
                LatLng latLng = marker.getPosition();
                if (latLng.latitude == mapMarkerOverlay.getLat() && latLng.longitude == mapMarkerOverlay.getLon()) {
//                    marker.remove();//移除旧的marker
//                    listMarkers.remove(i);
                    ifAddMarker = false;
                }
            }
            if(ifAddMarker || ifReDraw) {//金额小于1零豆为0不画点
                //定义Maker坐标点
                LatLng point = new LatLng(mapMarkerOverlay.getLat(), mapMarkerOverlay.getLon());
                //构建Marker图标
                try {
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
                    //构建MarkerOption，用于在地图上添加Marker
                    MarkerOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap)
                            .title(marketId)
                            .zIndex(mapMarkerOverlay.getMarketType() == 2?99:0);
                    option.animateType(MarkerOptions.MarkerAnimateType.grow);
                    //在地图上添加Marker，并显示
                    listMarkers.add((Marker) mBaiduMap.addOverlay(option));
                    bitmap.recycle();
                    view = null;


                } catch (OutOfMemoryError error) {
                    Log.d("slicejobs", "OutOfMemory");
                }
            }
        }
    }

    /**
     * 显示红色门店覆盖物
     */
    public void showRedUser(MapMarkerOverlay mapMarkerOverlay, String marketId) {
        if (null != mapMarkerOverlay) {
            TextView view = new TextView(this);
            int sum = (int) Math.round(mapMarkerOverlay.getSumMoney() - 0.5);
            if(sum >= 1) {
                if (StringUtil.isNotBlank(clickTaskMarketId) && clickTaskMarketId.equals(marketId)) {
                    view.setBackgroundResource(R.drawable.ic_markerpoint_bg_red);
                    view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                    view.setTextSize(9);
                    view.setGravity(Gravity.CENTER);
                    view.setPadding(0,0,0,DensityUtil.dip2px(this, 12));
                    if (sum <1000) {
                        view.setText(sum+"元");
                    } else {
                        view.setText((sum+"").substring(0,3)+"..\n元");
                    }
                } else {
                    if (mapMarkerOverlay.getMarketType() == 2) {
                        view.setBackgroundResource(R.drawable.ic_markerpoint_bg_flag_red);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                        view.setTextSize(8);
                        view.setPadding(DensityUtil.dip2px(this, 6),DensityUtil.dip2px(this, 8),0,0);
                        if (sum <100) {
                            view.setText(sum+"元");
                        } else {
                            view.setText((sum+"").substring(0,2)+"..元");
                        }
                    } else {
                        view.setBackgroundResource(R.drawable.ic_markerpoint_bg_red);
                        view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                        view.setTextSize(9);
                        view.setGravity(Gravity.CENTER);
                        view.setPadding(0,0,0,DensityUtil.dip2px(this, 12));
                        if (sum <1000) {
                            view.setText(sum+"元");
                        } else {
                            view.setText((sum+"").substring(0,3)+"..\n元");
                        }
                    }
                }
            }else {
                int pointSum = mapMarkerOverlay.getSumPoint();
                if(pointSum == 0){
                    view.setBackgroundResource(R.drawable.ic_markerpoint_bg_red);
                    view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                    view.setTextSize(9);
                    view.setGravity(Gravity.CENTER);
                    view.setPadding(0,0,0,DensityUtil.dip2px(this, 12));
                    view.setText("0元");
                }else {
                    view.setBackgroundResource(R.drawable.ic_task_point_red);
                    view.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_bg_red));
                }
            }
            //定义Maker坐标点
            LatLng point = new LatLng(mapMarkerOverlay.getLat(), mapMarkerOverlay.getLon());
            //构建Marker图标
            try {
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
                //构建MarkerOption，用于在地图上添加Marker
                MarkerOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .title(marketId)
                        .zIndex(0);
                option.animateType(MarkerOptions.MarkerAnimateType.grow);
                //在地图上添加Marker，并显示
                for(int i = 0;i < listMarkers.size();i++){
                    Marker marker = listMarkers.get(i);
                    LatLng latLng = marker.getPosition();
                    if(latLng.latitude == mapMarkerOverlay.getLat() && latLng.longitude == mapMarkerOverlay.getLon()){
                        marker.remove();//移除旧的marker
                        listMarkers.remove(i);
                    }
                }
                listMarkers.add((Marker) mBaiduMap.addOverlay(option));
                bitmap.recycle();
                view = null;


            } catch (OutOfMemoryError error) {
                Log.d("slicejobs", "OutOfMemory");
            }
        }
    }

    /**
     * 显示门店选中覆盖物
     */
    public void showMarketSelectedPoint(MapMarkerOverlay mapMarkerOverlay, String marketId) {
        if (null != mapMarkerOverlay) {
            TextView view = new TextView(this);
            view.setBackgroundResource(R.drawable.ic_task_point_sel);
            //定义Maker坐标点
            LatLng point = new LatLng(mapMarkerOverlay.getLat(), mapMarkerOverlay.getLon());
            //构建Marker图标
            try {
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
                //构建MarkerOption，用于在地图上添加Marker
                MarkerOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .title(marketId)
                        .zIndex(0);
                option.animateType(MarkerOptions.MarkerAnimateType.grow);
                //在地图上添加Marker，并显示
                for(int i = 0;i < listMarkers.size();i++){
                    Marker marker = listMarkers.get(i);
                    LatLng latLng = marker.getPosition();
                    if(latLng.latitude == mapMarkerOverlay.getLat() && latLng.longitude == mapMarkerOverlay.getLon()){
                        marker.remove();//移除旧的marker
                        listMarkers.remove(i);
                    }
                }
                listMarkers.add((Marker) mBaiduMap.addOverlay(option));
                bitmap.recycle();
                view = null;


            } catch (OutOfMemoryError error) {
                Log.d("slicejobs", "OutOfMemory");
            }
        }
    }

    @Override
    public void refreshTask(int start) {//提醒刷新

        if (start == 0 & !isLoadingMore) {//代表刷新
            startId = 0;
            isLoadingMore = true;
            loadAll = false;
            if (presenter != null && lat != 39.914884096217335 && lon != 116.40388321804957) {
                myLocationRadar.start();
                presenter.getNearbyTask(startId, lat + "", lon + "", distance, TaskPresenter.OrderBy.DISTANCE);
            }
        } else if (!isLoadingMore && !loadAll) {//代表加载更多
            isLoadingMore = true;
            if (presenter != null && lat != 39.914884096217335 && lon != 116.40388321804957) {
                myLocationRadar.start();
                presenter.getNearbyTwentyTask(startId, lat + "", lon + "", distance, TaskPresenter.OrderBy.DISTANCE);
            }
        }
    }

    private void getLocationTask() {
        startLocating();
    }

    @Override
    public void showTaskList(List<Task> taskList, int start) {
        handler.sendEmptyMessageDelayed(0,1500);
        isLoadingMore = false;
        if(!taskList.isEmpty()){
            for (int index = 0; index < taskList.size(); index++) {
                boolean isExist = false;
                for (int i = 0; i < taskListSum.size(); i++) {
                    if(taskList.get(index).getTaskid().equals(taskListSum.get(i).getTaskid())){
                        isExist = true;
                    }
                }
                if(!isExist){
                    if(currentBDLocation != null
                            && StringUtil.isNotBlank(taskList.get(index).getLatitude())
                            && StringUtil.isNotBlank(taskList.get(index).getLongitude())){
                        LatLng currentLatLng = new LatLng(currentBDLocation.getLatitude(),currentBDLocation.getLongitude());
                        LatLng taskLatLng = new LatLng(Double.parseDouble(taskList.get(index).getLatitude()), Double.parseDouble(taskList.get(index).getLongitude()));
                        double distance = DistanceUtil.getDistance(currentLatLng, taskLatLng) / 1000;
                        taskList.get(index).setDistance(distance + "");
                    }
                    taskListSum.add(taskList.get(index));
                }
            }
            Collections.sort(taskListSum, new TaskSortByDistance());
            mapNumMoney = 0;
            map.clear();
            for (int index = 0; index < taskListSum.size(); index++) {
                mapNumMoney += Float.parseFloat(taskListSum.get(index).getSalary());
                if (StringUtil.isNotBlank(taskListSum.get(index).getLatitude())  && StringUtil.isNotBlank(taskListSum.get(index).getLongitude())) {
                    MapMarkerOverlay mapMarkerOverlay = map.get(taskListSum.get(index).getMarketid());
                    if (null == mapMarkerOverlay) {//首次出现
                        map.put(taskListSum.get(index).getMarketid(), new MapMarkerOverlay(1, Float.parseFloat(taskListSum.get(index).getSalary()),
                                Integer.parseInt(taskListSum.get(index).getPoints()), Double.parseDouble(taskListSum.get(index).getLatitude()),
                                Double.parseDouble(taskListSum.get(index).getLongitude()), taskListSum.get(index).getMarketinfo().getAddress()));
                    } else {
                        //map.remove(taskListSum.get(index).getMarketid());
                        map.put(taskListSum.get(index).getMarketid(), new MapMarkerOverlay(1,mapMarkerOverlay.getSumMoney() + Float.parseFloat(taskListSum.get(index).getSalary()),
                                mapMarkerOverlay.getSumPoint() + Integer.parseInt(taskListSum.get(index).getPoints()), Double.parseDouble(taskListSum.get(index).getLatitude()),
                                Double.parseDouble(taskListSum.get(index).getLongitude()), taskListSum.get(index).getMarketinfo().getAddress()));
                    }
                }
            }
        }
        startId = start;
        updateSumMoney(mapNumMoney);
        if(!BizLogic.getCurrentUser().userid.equals(SliceStaticStr.VISITOR_ID)) {
            presenter.getMyTask(false, 0, "2,3");
        }else {
            //筛选之后根据门店画新的覆盖物
            if(addMarketPointTask != null && !addMarketPointTask.isCancelled()) {
                addMarketPointTask.cancel(true);
                addMarketPointTask = null;
                ivMyLocationPas.setVisibility(View.GONE);
                System.gc();
            }
            addMarketPointTask = new AddMarketPointTask();
            addMarketPointTask.execute();

            tvMapHint.setText(getString(R.string.hint_maptask_loadover, distance));
            //任务加载提示
            layoutMapHint.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.task_loader_msg);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    layoutMapHint.setVisibility(View.INVISIBLE);
                }
            });
            layoutMapHint.setAnimation(anim);
        }
    }


    /**
     * 更新地图上的总金额
     */
    public void updateSumMoney(float moneyNumber) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(Math.round(moneyNumber - 0.5) + "元");
        spannable.setSpan(new RelativeSizeSpan(1.0f), 0, spannable.toString().length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.toString().length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(0.6f), spannable.length() - 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSumMoney.setText(spannable);
    }

    @Override
    public void getTaskError() {
        isLoadingMore = false;
        myLocationRadar.stop();
    }

    @Override
    public void resetAccountDialog() {

    }

    @Override
    public void serverExecption(String source, TaskPresenter.OrderBy orderBy, int start, String status, boolean today, String latitude, String longitude, String disctance) {
        if (source.equals("getNearbyTask")) {
//            myLocationRadar.start();
//            presenter.getNearbyTask(latitude, longitude, mCurrentLongitude + "", mCurrentLantitude + "",disctance);
        }else if (source.equals("getMyTask")) {
            myLocationRadar.start();
            //筛选之后根据门店画新的覆盖物
            Set<String> list = map.keySet();
            for (String str : list) {
                MapMarkerOverlay mapMarkerOverlay = map.get(str);
                showUser(mapMarkerOverlay, str,false);
            }

            tvMapHint.setText(getString(R.string.hint_maptask_loadover, distance));
            //任务加载提示
            layoutMapHint.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.task_loader_msg);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    layoutMapHint.setVisibility(View.INVISIBLE);
                }
            });
            layoutMapHint.setAnimation(anim);
        }
    }

    @Override
    public void getMyTaskList(List<Task> taskList, int start) {
        for (int index = 0; index < taskList.size(); index++) {
            if(taskList.get(index).getMarketinfo() != null) {//判断我的任务是否为门店任务
                boolean isExist = false;
                for (int i = 0; i < taskListSum.size(); i++) {
                    if (taskList.get(index).getTaskid().equals(taskListSum.get(i).getTaskid())) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    taskListSum.add(taskList.get(index));
                    mapNumMoney += Float.parseFloat(taskList.get(index).getSalary());
                }
                MapMarkerOverlay mapMarkerOverlay = map.get(taskList.get(index).getMarketid());
                if (null == mapMarkerOverlay) {//首次出现
                    map.put(taskList.get(index).getMarketid(), new MapMarkerOverlay(2, Float.parseFloat(taskList.get(index).getSalary()), Integer.parseInt(taskList.get(index).getPoints()), Double.parseDouble(taskList.get(index).getLatitude()),
                            Double.parseDouble(taskList.get(index).getLongitude()), taskList.get(index).getMarketinfo().getAddress()));
                } else {
                    //map.remove(taskList.get(index).getMarketid());
                    map.put(taskList.get(index).getMarketid(), new MapMarkerOverlay(2, mapMarkerOverlay.getSumMoney() + Float.parseFloat(taskList.get(index).getSalary()),
                            mapMarkerOverlay.getSumPoint() + Integer.parseInt(taskList.get(index).getPoints()), Double.parseDouble(taskList.get(index).getLatitude()),
                            Double.parseDouble(taskList.get(index).getLongitude()), taskList.get(index).getMarketinfo().getAddress()));
                }
            }
        }
        if (mapTaskListFragment != null) {
            mapTaskListFragment.updateMapTasks(this.taskListSum);
        }
        //筛选之后根据门店画新的覆盖物
        if(addMarketPointTask != null && !addMarketPointTask.isCancelled()) {
            addMarketPointTask.cancel(true);
            addMarketPointTask = null;
            ivMyLocationPas.setVisibility(View.GONE);
            System.gc();
        }
        addMarketPointTask = new AddMarketPointTask();
        addMarketPointTask.execute();

        tvMapHint.setText(getString(R.string.hint_maptask_loadover, distance));
        //任务加载提示
        layoutMapHint.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.task_loader_msg);
        anim.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                layoutMapHint.setVisibility(View.INVISIBLE);
            }
        });
        layoutMapHint.setAnimation(anim);
        updateSumMoney(mapNumMoney);
    }

    @Override
    public void showKeywordTaskList(List<Task> taskList) {
        if(taskList.size() != 0){
            rvNearTaskList.setVisibility(View.VISIBLE);
            frameNoData.setVisibility(View.GONE);
            taskListAdapter.updateTasks(taskList);
        } else {
            frameNoData.setVisibility(View.VISIBLE);
            rvNearTaskList.setVisibility(View.GONE);
        }
    }

    /*@Override
    public void showProgressDialog() {

    }

    @Override
    public void dismissProgressDialog() {

    }*/


    class MyMapMoveListener implements BaiduMap.OnMapStatusChangeListener{

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) { //开始移动
            if (addMarketPointTask != null && !addMarketPointTask.isCancelled()) {
                addMarketPointTask.cancel(true);
                addMarketPointTask = null;
                ivMyLocationPas.setVisibility(View.GONE);
                System.gc();
            }
            handler.sendEmptyMessage(0);
            ivMyLocationPas.setVisibility(View.VISIBLE);
            try {
                if (myMarker != null) {
                    myMarker.remove();
                    myMarker = null;
                }
            /*for (Marker marker : listMarkers) {
                marker.remove();
            }
            mBaiduMap.clear();
            listMarkers.clear();*/
            } catch (Exception e) {
                Log.d("RimTaskMapActivity", "null");
            }
        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) { //移动中

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) { //结束移动
            LatLng mCenterLatLng = mapStatus.target;
            lat = mCenterLatLng.latitude;
            lon = mCenterLatLng.longitude;
            zoom = mapStatus.zoom;

            showUserMarket();
            if (!isPlanRoute) {
                if (presenter != null && lat != 39.914884096217335 && lon != 116.40388321804957) {
                    if (getMapRadius() != -1) {
                        if (getMapRadius() >= 20) {
                            distance = 20 + "";
                        } else {
                            distance = getMapRadius() + "";
                        }
                    }
                    startId = 0;
                    isLoadingMore = true;
                    loadAll = false;
                    handler.sendEmptyMessageDelayed(1, 1500);
                    presenter.getNearbyTask(lat + "", lon + "", mCurrentLongitude + "", mCurrentLantitude + "", distance);
                }
            }
        }
    }


    /**
     * 显示用户覆盖物
     */
    public void showUserMarket() {
        if(myMarker == null) {
            LatLng cenpt = new LatLng(lat, lon);
            myOptions = new MarkerOptions()
                    .position(cenpt)  //设置位置
                    .icon(descriptor)  //设置图标
                    .zIndex(9)//设置r所在层级
                    .title(MY_LOCATION)
                    .draggable(false);//设置手势拖拽
            myOptions.animateType(MarkerOptions.MarkerAnimateType.jump);
            myOptions.anchor(0.5f, 1.0f);
            myMarker = (Marker) mBaiduMap.addOverlay(myOptions);
            myMarker.setToTop();
            //myLocationRadar.setRepeat(1);
            //myLocationRadar.start();
        }
        ivMyLocationPas.setVisibility(View.GONE);
    }

    public void setFlagImageClick() {

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {//筛选门店任务
                //恢复上一个点击门店的颜色
                boolean ifRepeatClick = false;
                if(!clickList.isEmpty()){
                    boolean ifLastMarketInRoutePlans = false;//上次点击的点是否在多门店路线规划中
                    Task task = clickList.get(0);
                    String lastClickMarketId = task.getMarketid();
                    MapMarkerOverlay lastMarkerOverlay = map.get(lastClickMarketId);
                    if (lastMarkerOverlay != null) {
                        if (marker.getPosition().latitude == lastMarkerOverlay.getLat() || marker.getPosition().longitude == lastMarkerOverlay.getLon()) {
                            ifRepeatClick = true;
                        }
                        for (int i = 0; i < routePlanList.size(); i++) {
                            LatLng latLng = routePlanList.get(i);
                            if (latLng.latitude == lastMarkerOverlay.getLat() || latLng.longitude == lastMarkerOverlay.getLon()) {
                                ifLastMarketInRoutePlans = true;
                            }
                        }
                    }

                    if (!ifLastMarketInRoutePlans && !ifRepeatClick) {
                        showUser(lastMarkerOverlay,lastClickMarketId,true);
                    }
                }

                MapMarkerOverlay mapMarkerOverlay = map.get(marker.getTitle());
                if (mapMarkerOverlay != null) {
                    boolean ifInRoutePlans = false;//当前点击的点是否在多门店路线规划中
                    for (int i = 0; i < routePlanList.size(); i++) {
                        LatLng latLng = routePlanList.get(i);
                        if (latLng.latitude == mapMarkerOverlay.getLat() || latLng.longitude == mapMarkerOverlay.getLon()) {
                            ifInRoutePlans = true;
                        }
                    }
                    if(!ifInRoutePlans && !ifRepeatClick){
                        showRedUser(mapMarkerOverlay,marker.getTitle());
                    }
                }
                clickList.clear();
                for (Task task : taskListSum) {
                    if (StringUtil.isNotBlank(task.getMarketid()) && task.getMarketid().equals(marker.getTitle())) {
                        clickList.add(task);
                    }
                }
                try {
                    if (null != mapTaskListFragment && null != marker && StringUtil.isNotBlank(marker.getTitle()) && !marker.getTitle().equals(MY_LOCATION) && clickList.size() != 0) {
                        mapTaskListFragment.updateMapTasks(clickList);
                        mapTaskListFragment.updateRoutePlanMarkets(routePlanList);
                        mapTaskListFragment.showOrDismiss(marker.getTitle(),marker.getPosition(),clickList.get(0).getMarketinfo().getAddress(),clickList.get(0).getMarketinfo().getMarketname());
                        /*boolean if_first_plan_route = SliceApp.PREF.getBoolean("IF_FIRST_USE_ROUTE_PLAN", true);
                        if(if_first_plan_route) {
                            addMarketHelpFrame.setVisibility(View.VISIBLE);
                        }*/
                    }
                } catch (NullPointerException e) {

                }
//                TextView textView = new TextView(MapActivity.this);
//                textView.setBackgroundResource(R.drawable.shape_toast_layout_bg);
//                textView.setPadding(DensityUtil.dip2px(MapActivity.this, 20), 0, DensityUtil.dip2px(MapActivity.this, 20), 0);
//                textView.setText(title);
//                //(2)定义用于显示该InfoWindow的坐标
//                LatLng pt = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
//                //(3)创建InfoWindow,传入VIew，地理坐标，y轴偏移量
//                InfoWindow mInfoWindow = new InfoWindow(textView, pt, DensityUtil.dip2px(SliceApp.CONTEXT, -47));//-47
//                //(4)显示InfoWindow
//                mBaiduMap.showInfoWindow(mInfoWindow);
                return false;
            }
        });

    }


    public static class AnimationListenerAdapter implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private boolean ifPointInScreen(double lat,double lon){
        if(mBaiduMap != null) {
            Point leftTopPoint = new Point();
            leftTopPoint.x = 0;
            leftTopPoint.y = 43;
            Point rightBottomPoint = new Point();
            rightBottomPoint.x = DensityUtil.screenWidthInPix(this);
            rightBottomPoint.y = DensityUtil.screenHeightInPix(this);
            Projection mProjection = mBaiduMap.getProjection();
            if(mProjection != null) {
                LatLng leftTopLatlng = mProjection.fromScreenLocation(leftTopPoint);
                LatLng rightBottomLatlng = mProjection.fromScreenLocation(rightBottomPoint);
                if (lat > rightBottomLatlng.latitude && lat < leftTopLatlng.latitude
                        && lon > leftTopLatlng.longitude && lon < rightBottomLatlng.longitude) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /*
    * 获取地图半径(屏幕左上角与中心的距离)
    * */
    private double getMapRadius(){
        Projection mProjection = mBaiduMap.getProjection();
        if(mProjection != null) {
            Point leftTopPoint = new Point();
            leftTopPoint.x = 0;
            leftTopPoint.y = 43;
            Point centerPoint = new Point();
            centerPoint.x = DensityUtil.screenWidthInPix(this) / 2;
            centerPoint.y = (DensityUtil.screenHeightInPix(this) - 43) / 2;
            LatLng leftTopLatlng = mProjection.fromScreenLocation(leftTopPoint);
            LatLng centerLatlng = mProjection.fromScreenLocation(centerPoint);
            int radius = (int) (DistanceUtil.getDistance(centerLatlng, leftTopLatlng) / 1000);
            if(radius == 0){
                radius = 1;
            }
            return radius;
        }
        return -1;
    }

    private class AddMarketPointTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myLocationRadar.start();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Set<String> list = map.keySet();
            List<Marker> tampMarkers = new ArrayList<>();
            try {
                for (String str : list) {
                    boolean ifMarketExist = false;
                    for (int i = 0; i < listMarkers.size(); i++) {
                        Marker marker = listMarkers.get(i);
                        if (marker.getTitle().equals(str)) {
                            ifMarketExist = true;
                        }
                        if (!ifPointInScreen(marker.getPosition().latitude, marker.getPosition().longitude)) {//不在可见范围内移除
                            tampMarkers.add(marker);
                            marker.remove();
                        }
                    }
                    if (!ifMarketExist) {
                        MapMarkerOverlay mapMarkerOverlay = map.get(str);
                        if (mapMarkerOverlay != null) {
                            if (ifPointInScreen(mapMarkerOverlay.getLat(), mapMarkerOverlay.getLon())) {
                                showUser(mapMarkerOverlay, str,false);
                            }
                        }
                    }
                    listMarkers.removeAll(tampMarkers);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            List<Task> tampList = new ArrayList<>();
            for (int i = 0; i < taskListSum.size(); i++) {
                if (!ifPointInScreen(Double.parseDouble(taskListSum.get(i).getLatitude()), Double.parseDouble(taskListSum.get(i).getLongitude()))) {
                    tampList.add(taskListSum.get(i));
                    mapNumMoney -= Float.parseFloat(taskListSum.get(i).getSalary());
                    if(mapNumMoney < 0){
                        mapNumMoney = 0;
                    }

                }
            }
            taskListSum.removeAll(tampList);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            handler.sendEmptyMessageDelayed(0,1000);
            updateSumMoney(mapNumMoney);
        }
    }

    private void changeMapZoom(float zoom){
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(zoom);
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                myLocationRadar.stop();
            }else if(msg.what == 1){
                myLocationRadar.start();
            }
        }
    };

    /*
    * 路线规划
    * */
    private void planRouteToMarket(int routePlan){
        showProgressDialog();
        if(transitRouteOverlay != null) {
            transitRouteOverlay.removeFromMap();
        }
        if(drivingRouteOverlay != null) {
            drivingRouteOverlay.removeFromMap();
        }
        if(walkingRouteOverlay != null) {
            walkingRouteOverlay.removeFromMap();
        }
        if(startLatlng == null || endLatlng == null){
            return;
        }
        if(mRPSearch == null) {
            mRPSearch = RoutePlanSearch.newInstance();
        }
        mRPSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        PlanNode stNode = PlanNode.withLocation(startLatlng);
        PlanNode enNode = PlanNode.withLocation(endLatlng);
        switch (routePlan){
            case ROUTE_PLAN_DIRVING:
                if(ifFirstPlanRoute){
                    ifFirstPlanRoute = false;
                }
                routePlanDrive.setBackgroundResource(R.drawable.ic_drive_select);
                routePlanBus.setBackgroundResource(R.drawable.ic_bus_nomal);
                routePlanWalk.setBackgroundResource(R.drawable.ic_walk_nomal);
                routePlanDriveMin.setTextColor(getResources().getColor(R.color.text_sel_color));
                routePlanBusMin.setTextColor(getResources().getColor(R.color.text_color5));
                routePlanWalkMin.setTextColor(getResources().getColor(R.color.text_color5));
                mRPSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(stNode).to(enNode));
                break;
            case ROUTE_PLAN_WALKING:
                if(ifFirstPlanRoute){
                    ifFirstPlanRoute = false;
                }
                routePlanDrive.setBackgroundResource(R.drawable.ic_drive_nomal);
                routePlanBus.setBackgroundResource(R.drawable.ic_bus_nomal);
                routePlanWalk.setBackgroundResource(R.drawable.ic_walk_select);
                routePlanDriveMin.setTextColor(getResources().getColor(R.color.text_color5));
                routePlanBusMin.setTextColor(getResources().getColor(R.color.text_color5));
                routePlanWalkMin.setTextColor(getResources().getColor(R.color.text_sel_color));
                mRPSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(stNode).to(enNode));
                break;
            case ROUTE_PLAN_BUS:
                routePlanDrive.setBackgroundResource(R.drawable.ic_drive_nomal);
                routePlanBus.setBackgroundResource(R.drawable.ic_bus_select);
                routePlanWalk.setBackgroundResource(R.drawable.ic_walk_nomal);
                routePlanDriveMin.setTextColor(getResources().getColor(R.color.text_color5));
                routePlanBusMin.setTextColor(getResources().getColor(R.color.text_sel_color));
                routePlanWalkMin.setTextColor(getResources().getColor(R.color.text_color5));
                GeoCoder geoCoder = GeoCoder.newInstance();
                geoCoder.setOnGetGeoCodeResultListener(this);
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(endLatlng));
                if(ifFirstPlanRoute){//第一次进来规划路线，也规划驾车和步行
                    mRPSearch.drivingSearch((new DrivingRoutePlanOption())
                            .from(stNode).to(enNode));
                    mRPSearch.walkingSearch((new WalkingRoutePlanOption())
                            .from(stNode).to(enNode));
                }
                break;
        }
        mRPSearch.destroy();
        currentRoutePlan = routePlan;
    }

    private void multiMarketPlanRoute(){
        currentRoutePlan = ROUTE_PLAN_DIRVING;
        ifMultiPlan = true;
        mapTaskListFragment.setOnRoutePlan(true);
        showProgressDialog();
        if(mRPSearch == null) {
            mRPSearch = RoutePlanSearch.newInstance();
        }
        mRPSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        startLatlng = new LatLng(mCurrentLantitude,mCurrentLongitude);
        PlanNode stNode = PlanNode.withLocation(startLatlng);
        PlanNode enNode;
        DrivingRoutePlanOption drivingRoutePlanOption = new DrivingRoutePlanOption();
        if (routePlanList.size() == 1) {
            enNode = PlanNode.withLocation(routePlanList.get(0));
        }else {
            int fartestIndex = 0;
            double distance = 0;
            for (int i = 0; i < routePlanList.size(); i++) {
                double tampDistance = DistanceUtil.getDistance(startLatlng, routePlanList.get(i));
                if (tampDistance > distance) {
                    distance = tampDistance;
                    fartestIndex = i;
                }
            }
            enNode = PlanNode.withLocation(routePlanList.get(fartestIndex));
            List<PlanNode> planNodeList = new ArrayList<>();
            for (int i = 0; i < routePlanList.size(); i++) {
                if (i != fartestIndex) {
                    PlanNode planNode = PlanNode.withLocation(routePlanList.get(i));
                    planNodeList.add(planNode);
                }
            }
            drivingRoutePlanOption.passBy(planNodeList);
        }
        mRPSearch.drivingSearch((drivingRoutePlanOption)
                .from(stNode).to(enNode));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            dismissProgressDialog();
            toast("抱歉，未找到公交路线");
            routePlanBusMin.setText("无路线");
            routeDiatanceLayout.setVisibility(View.GONE);
        }else {
            if(currentBDLocation != null && currentBDLocation.getCityCode() != null && StringUtil.isNotBlank(currentBDLocation.getAddrStr()) && StringUtil.isNotBlank(currentBDLocation.getCity())){
                PlanNode stTransitNode = PlanNode.withCityCodeAndPlaceName(Integer.parseInt(currentBDLocation.getCityCode()),currentBDLocation.getAddrStr());
                PlanNode enTransitNode = PlanNode.withCityCodeAndPlaceName(reverseGeoCodeResult.getCityCode(),reverseGeoCodeResult.getAddress());
                if(mRPSearch == null) {
                    mRPSearch = RoutePlanSearch.newInstance();
                }
                mRPSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
                mRPSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stTransitNode)
                        .to(enTransitNode)
                        .city(currentBDLocation.getCity()));
                mRPSearch.destroy();
            }else {
                dismissProgressDialog();
                toast("抱歉，未找到公交路线");
                routePlanBusMin.setText("无路线");
                routeDiatanceLayout.setVisibility(View.GONE);
            }
        }
    }

    private OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            dismissProgressDialog();
            if(walkingRouteOverlay == null) {
                walkingRouteOverlay = new WalkingRouteOverlay(mBaiduMap);
            }
            if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR
                    || walkingRouteResult.getRouteLines() == null || walkingRouteResult.getRouteLines().size() == 0) {
                if(!ifFirstPlanRoute) {
                    toast("抱歉，未找到步行路线");
                }
                routePlanWalkMin.setText("无路线");
                routeDiatanceLayout.setVisibility(View.GONE);
                return;
            }
            if(walkingRouteResult.getRouteLines().size() > 0){
                WalkingRouteLine walkingRouteLine = walkingRouteResult.getRouteLines().get(0);
                int duration = walkingRouteLine.getDuration();
                routePlanWalkMin.setText(duration/60 + "分钟");
                if(!ifFirstPlanRoute) {
                    walkingRouteOverlay.setData(walkingRouteLine);
                    //在地图上绘制DrivingRouteOverlay
                    walkingRouteOverlay.addToMap();
                    walkingRouteOverlay.zoomToSpan();

                    routeDiatanceLayout.setVisibility(View.VISIBLE);
                    String distanceKm = getDistanceText(walkingRouteLine.getDistance());
                    routeDiatanceText.setText(distanceKm);
                }
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            dismissProgressDialog();
            if(transitRouteOverlay == null) {
                transitRouteOverlay = new TransitRouteOverlay(mBaiduMap);
            }
            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR
                    || transitRouteResult.getRouteLines() == null || transitRouteResult.getRouteLines().size() == 0) {
                toast("抱歉，未找到公交路线");
                routePlanBusMin.setText("无路线");
                routeDiatanceLayout.setVisibility(View.GONE);
                return;
            }
            if(transitRouteResult.getRouteLines().size() > 0){
                if (cardPagerAdapter == null) {
                    cardPagerAdapter = new CardPagerAdapter();
                }
                cardPagerAdapter.clearCardItem();
                List<TransitRouteLine> routeLines = transitRouteResult.getRouteLines();
                for (int i = 0; i < routeLines.size(); i++) {
                    TransitRouteLine transitRouteLine = routeLines.get(i);
                    cardPagerAdapter.addCardItem(transitRouteLine);
                }
                shadowTransformer = new ShadowTransformer(routePlanBusVp, cardPagerAdapter);
                shadowTransformer.enableScaling(true);
                routePlanBusVp.setAdapter(cardPagerAdapter);
                routePlanBusVp.setPageTransformer(false, shadowTransformer);
                routePlanBusVp.setOffscreenPageLimit(3);

                routePlanBusVp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        transitRouteOverlay.setData(transitRouteResult.getRouteLines().get(position));
                        //在地图上绘制DrivingRouteOverlay
                        transitRouteOverlay.addToMap();
                        transitRouteOverlay.zoomToSpan();
                        routePlanBusMin.setText(transitRouteResult.getRouteLines().get(position).getDuration() / 60 + "分钟");
                        routeDiatanceLayout.setVisibility(View.VISIBLE);
                        String distanceKm = getDistanceText(transitRouteResult.getRouteLines().get(position).getDistance());
                        routeDiatanceText.setText(distanceKm);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                transitRouteOverlay.setData(transitRouteResult.getRouteLines().get(routePlanBusVp.getCurrentItem()));
                //在地图上绘制DrivingRouteOverlay
                transitRouteOverlay.addToMap();
                transitRouteOverlay.zoomToSpan();

                routeDiatanceLayout.setVisibility(View.VISIBLE);
                String distanceKm = getDistanceText(transitRouteResult.getRouteLines().get(routePlanBusVp.getCurrentItem()).getDistance());
                routeDiatanceText.setText(distanceKm);
                routePlanBusMin.setText(transitRouteResult.getRouteLines().get(routePlanBusVp.getCurrentItem()).getDuration() / 60 + "分钟");
            }
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            dismissProgressDialog();
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR
                    || drivingRouteResult.getRouteLines() == null || drivingRouteResult.getRouteLines().size() == 0) {
                if(!ifFirstPlanRoute) {
                    toast("抱歉，未找到驾车路线");
                }
                routePlanDriveMin.setText("无路线");
                routeDiatanceLayout.setVisibility(View.GONE);
                return;
            }
            mapTitle.setVisibility(View.GONE);
            routePlanLayout.setVisibility(View.VISIBLE);
            myLocationRadar.setVisibility(View.GONE);
            if(drivingRouteOverlay == null) {
                drivingRouteOverlay = new DrivingRouteOverlay(mBaiduMap);
            }
            if(drivingRouteResult.getRouteLines().size() > 0){
                DrivingRouteLine drivingRouteLine = drivingRouteResult.getRouteLines().get(0);
                int duration = drivingRouteLine.getDuration();
                routePlanDriveMin.setText(duration/60 + "分钟");
                if(!ifFirstPlanRoute) {
                    routeDiatanceLayout.setVisibility(View.VISIBLE);
                    String distanceKm = getDistanceText(drivingRouteLine.getDistance());
                    routeDiatanceText.setText(distanceKm);
                    drivingRouteOverlay.setData(drivingRouteLine);
                    //在地图上绘制DrivingRouteOverlay
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                }
                if (ifMultiPlan) {
                    multiRoutePlanReview.setVisibility(View.VISIBLE);
                    allTaskLayout.setVisibility(View.GONE);
                    rpBusLayout.setVisibility(View.GONE);
                    rpWalkLayout.setVisibility(View.GONE);
                }else {
                    rpBusLayout.setVisibility(View.VISIBLE);
                    rpWalkLayout.setVisibility(View.VISIBLE);
                    multiRoutePlanReview.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }

    };

    @Override
    public void onMarketRoutePlan(LatLng marketLatlng) {
        endLatlng = marketLatlng;
        startLatlng = new LatLng(mCurrentLantitude,mCurrentLongitude);
        mapTitle.setVisibility(View.GONE);
        routePlanLayout.setVisibility(View.VISIBLE);
        ifMultiPlan = false;
        mapTaskListFragment.setOnRoutePlan(true);
        planRouteToMarket(ROUTE_PLAN_DIRVING);
    }

    @Override
    public void onAddMarketToPlan(String marketId, LatLng marketLatlng) {
        boolean ifExist = false;
        for (int i = 0; i < routePlanList.size(); i++) {
            LatLng latLng = routePlanList.get(i);
            if(latLng.longitude == marketLatlng.longitude || latLng.longitude == marketLatlng.longitude) {
                ifExist = true;
            }
        }
        if (!ifExist) {
            routePlanList.add(marketLatlng);
            MapMarkerOverlay mapMarkerOverlay = map.get(marketId);
            showMarketSelectedPoint(mapMarkerOverlay, marketId);
        }
        if (routePlanList.size() > 0) {
            multiRoutePlan.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRemoveMarketToPlan(String marketId, LatLng marketLatlng) {
        for (int i = 0; i < routePlanList.size(); i++) {
            LatLng latLng = routePlanList.get(i);
            if(latLng.longitude == marketLatlng.longitude || latLng.longitude == marketLatlng.longitude) {
                routePlanList.remove(i);
                for(int k = 0;k < listMarkers.size();k++){
                    Marker marker = listMarkers.get(k);
                    LatLng markerLL = marker.getPosition();
                    if(markerLL.latitude == marketLatlng.longitude || markerLL.longitude == marketLatlng.longitude){
                        marker.remove();//移除旧的marker
                        listMarkers.remove(k);
                    }
                }
                MapMarkerOverlay mapMarkerOverlay = map.get(marketId);
                showUser(mapMarkerOverlay, marketId,true);
            }
        }
        if (routePlanList.size() == 0) {
            multiRoutePlan.setVisibility(View.GONE);
        }
    }

    private class TaskSortByDistance implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Task task1 = (Task) o1;
            Task task2 = (Task) o2;
            if(Double.parseDouble(task1.getDistance()) > Double.parseDouble(task2.getDistance())){
                return 1;
            }
            return -1;
        }
    }

    private void setMapCenterPoint(){
        //地图移动中心点
        lat = mCurrentLantitude;
        lon = mCurrentLongitude;
        zoom = 15;
        LatLng cenpt = new LatLng(lat, lon);
        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(zoom).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        try {
            mBaiduMap.setMapStatus(mMapStatusUpdate);
        } catch (NullPointerException e) {
            Log.d("slicejobs", "baidu accidental null pointer");
        }

        try {
            if (myMarker != null) {
                myMarker.remove();
                myMarker = null;
            }
        } catch (Exception e) {
            Log.d("RimTaskMapActivity", "null");
        }

        //设置当前位置的地图坐标
        myOptions = new MarkerOptions()
                .position(cenpt)  //设置位置
                .icon(descriptor)  //设置图标
                .zIndex(9)  //设置r所在层级
                .title(MY_LOCATION)
                .draggable(false);  //设置手势拖拽
        myOptions.anchor(0.5f, 1.0f);
        myOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
        myMarker = (Marker) mBaiduMap.addOverlay(myOptions);
        myMarker.setToTop();
    }

    private String getDistanceText (int distanceM) {
        if (distanceM < 1000) {
            return "路程" + distanceM + "m";
        } else {
            return "路程" + distanceM / 1000 + "." + distanceM % 1000 + "km";
        }
    }
}
