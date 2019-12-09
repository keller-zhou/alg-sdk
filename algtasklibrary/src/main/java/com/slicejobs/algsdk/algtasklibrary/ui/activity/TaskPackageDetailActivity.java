package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.taobao.weex.WXSDKInstance;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;


public class TaskPackageDetailActivity extends BaseActivity implements IJsRenderListener {


    @BindView(R2.id.taskpackage_view)
    RelativeLayout taskPackageView;
    WXSDKInstance mWXSDKInstance;
    private StringBuilder initJsonData = null;
    private String packageid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskpackage_detail);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        packageid = getIntent().getStringExtra("packageid");
        initJsonData = new StringBuilder();
        initJsonData.append("{");
        initJsonData.append("\"packageid\":\"").append(packageid).append("\"");
        initJsonData.append("}");
        renderJs(AppConfig.TASK_PACKAGE_DETAIL,initJsonData.toString(),"任务包详情",this);
    }

    @OnClick({R2.id.action_go_back})
    public void onClick(View view) {
        if (view.getId() == R.id.action_go_back) {
            finish();
        }
    }


    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }


    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        taskPackageView.addView(view);
    }

}
