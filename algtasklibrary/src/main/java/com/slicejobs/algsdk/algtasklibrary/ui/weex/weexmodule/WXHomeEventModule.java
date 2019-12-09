package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.hjq.toast.ToastUtils;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.Market;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.TaskWebDetailActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ActionSheetDialog;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.OpenLocalMapUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.schedulers.Schedulers;

public class WXHomeEventModule extends WXModule {

    //分步任务接口
    @WXModuleAnno
    public void taskStepViewEvent(String enventType, Map<String, Object> params) {
        BusProvider.getInstance().post(new AppEvent.TaskStepViewEvent(enventType, params));
    }

    @WXModuleAnno
    public void getStillLocation(String no, String callbackId) {
        //局部回调
        BDLocation mBDLocation = (BDLocation) PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getObject(AppConfig.USER_LOCATION_INFO,BDLocation.class);
        if(mBDLocation != null) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("latitude", mBDLocation.getLatitude());
            result.put("longitude", mBDLocation.getLongitude());
            result.put("city", mBDLocation.getCity());
            result.put("district", mBDLocation.getDistrict());
            result.put("addStr", mBDLocation.getAddrStr());
            result.put("radius", mBDLocation.getRadius());
            result.put("networkLocationType", mBDLocation.getNetworkLocationType());
            WXBridgeManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, result);
        }
    }

    //任务详情接口
    @WXModuleAnno
    public void taskDetailViewEvent(String enventType, Map<String, Object> params) {
        BusProvider.getInstance().post(new AppEvent.TaskDetailViewEvent(enventType, params));
    }

    //打开web detail界面
    @WXModuleAnno
    public void openTaskWebDetail(Map<String, Object> params) {
        Intent intent = new Intent(mWXSDKInstance.getContext(), TaskWebDetailActivity.class);
        Bundle bundle = new Bundle();
        SerializableBaseMap tmpmap=new SerializableBaseMap();
        tmpmap.setMap(params);
        bundle.putSerializable("weex_data", tmpmap);
        intent.putExtras(bundle);

        mWXSDKInstance.getContext().startActivity(intent);
    }

    @WXModuleAnno
    public void openNavigation(Market market) {
        new ActionSheetDialog(mWXSDKInstance.getContext())
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("百度地图", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                MobclickAgent.onEvent(SliceApp.CONTEXT, "um_function_map_task_navigation_start_baidu");
                                openBaiduNavigation(market);
                            }
                        })
                .addSheetItem("高德地图", ActionSheetDialog.SheetItemColor.Black,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                MobclickAgent.onEvent(SliceApp.CONTEXT, "um_function_map_task_navigation_start_gaode");
                                openGaodeNavigation(market);
                            }
                        }).show();
    }

    //自定义toast
    @WXModuleAnno
    public void showMessageWithTitle(String message, int duration){
        ToastUtils.show(message);
    }

    @WXModuleAnno
    public void openBaiduNavigation(Market market) {//去我的任务
        if(market != null) {
            if (OpenLocalMapUtil.isBaiduMapInstalled()) {
                try {
                    String uri = OpenLocalMapUtil.getBaiduMapUri(market.getAddress(), "com.slicejobs.algsdk.algtasklibrary");
                    Intent intent = Intent.parseUri(uri, 0);
                    mWXSDKInstance.getContext().startActivity(intent); //启动调用
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showMessageWithTitle("您尚未安装百度地图，请安装百度地图后使用此功能", 1000);
            }
        }
    }

    @WXModuleAnno
    public void openGaodeNavigation(Market market) {//打开高德导航
        if(market != null) {
            if (OpenLocalMapUtil.isGdMapInstalled()) {
                try {
//                    double bdLat = Double.parseDouble(market.getLatitude());
//                    double bdLon = Double.parseDouble(market.getLongitude());
//                    double[] gaodeGps = OpenLocalMapUtil.bdToGaoDe(bdLat, bdLon);
                    String uri = OpenLocalMapUtil.getGdMapUri("爱零工", market.getAddress());
                    Intent intent = Intent.parseUri(uri, 0);
                    mWXSDKInstance.getContext().startActivity(intent); //启动调用
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showMessageWithTitle("您尚未安装高德地图，请安装高德地图后使用此功能", 1000);
            }
        }
    }

    @WXModuleAnno
    public void evidenceManager(Map<String, Object> params) {
        if(params.get("taskId") != null) {
            String orderId = params.get("taskId").toString();
            User user = BizLogic.getCurrentUser();

            String tempFile = AppConfig.TEMP_CACHE_DIR + File.separator + user.userid + "-" + orderId;

            String cacheFile = AppConfig.LONG_CACHE_DIR + File.separator + user.userid + "-" + orderId;

            final File dir;

            File dir1 = new File(tempFile);
            File dir2 = new File(cacheFile);
            if (dir1.exists()) {
                dir = dir1;
            } else {
                dir = dir2;
            }

            if (dir.exists()) {//如果有缓存目录,结束时间有效
                Observable.create(subscriber -> {
                    try {
                        if (dir.isDirectory()) {
                            for (File f : dir.listFiles()) {
                                if (f.isDirectory()) {
                                    for (File f2 : f.listFiles()) {
                                        f2.delete();
                                    }
                                }
                                f.delete();
                            }
                            dir.delete();
                        }
                    } catch (Exception e) {
                        subscriber.onNext(null);
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe();
            }
        }
    }

    @WXModuleAnno
    public void taskEvaluateFinished() {//任务评价完成
        if ( mWXSDKInstance.getContext() instanceof Activity) {
            Activity activity = (Activity) mWXSDKInstance.getContext();
            activity.finish();
        }
        BusProvider.getInstance().post(new AppEvent.TaskDetailViewEvent("taskEvaluateFinished", null));
    }
}
