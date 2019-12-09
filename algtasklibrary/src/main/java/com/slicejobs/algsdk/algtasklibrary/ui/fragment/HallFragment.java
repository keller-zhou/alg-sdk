package com.slicejobs.algsdk.algtasklibrary.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nlmartian on 7/24/15.
 */
public class HallFragment extends BaseFragment implements IJsRenderListener {

    @BindView(R2.id.hall_task_layout)
    RelativeLayout hallTaskLayout;

    WXSDKInstance mWXSDKInstance;
    public static HallFragment newInstance() {
        HallFragment hallFragment = new HallFragment();
        return hallFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hall, container, false);
        ButterKnife.bind(this, view);

        renderJs(hallTaskLayout, AppConfig.TASK_LIST_STORE_VIEW_FILE,null,"门店任务",this);
        return view;
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        hallTaskLayout.addView(view);
    }

    @Subscribe
    public void onRefreshTaskEvent(AppEvent.RefreshTaskEvent event) {
        if (StringUtil.isNotBlank(event.status) && event.status.equals("store_task_list")) {
            Map<String, Object> params = new HashMap<>();
            params.put("updateType", "refreshTask");
            if (null != mWXSDKInstance) {
                mWXSDKInstance.fireGlobalEventCallback("storeTaskListChange", params);
            }
        }
    }

}
