package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddMarketActivity extends BaseActivity implements IJsRenderListener {
    private static final String FROM_SETTING = "from_setting";

    @BindView(R2.id.market_count_layout)
    LinearLayout marketCountLayout;
    @BindView(R2.id.watched_count)
    TextView watchedCount;
    @BindView(R2.id.watched_max_count)
    TextView watchMaxCount;
    @BindView(R2.id.web_market_layout)
    RelativeLayout marketLayout;
    WXSDKInstance mWXSDKInstance;

    public static Intent startFromSetting(Context context) {
        Intent intent = new Intent(context, AddMarketActivity.class);
        intent.putExtra(FROM_SETTING, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_market);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        renderJs(AppConfig.MODIFY_MARKET_VIEW_FILE,null,"关注门店",this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @OnClick({R2.id.action_go_back})
    public void onClick(View view) {
        if (view.getId() == R.id.action_go_back) {
            finish();
        }
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        marketLayout.addView(view);
    }

    @Subscribe
    public void onModifyMarketViewEvent(AppEvent.ModifyMarketViewEvent event) {
        if (event.params != null) {
            int watchCount = 0,watchMax = 10;
            if (event.params.get("in_watch") != null) {
                watchCount = (int) event.params.get("in_watch");
            }
            if (event.params.get("max") != null) {
                watchMax = (int) event.params.get("max");
            }
            marketCountLayout.setVisibility(View.VISIBLE);
            watchedCount.setText(watchCount + "");
            watchMaxCount.setText(watchMax + "");
        }
    }
}
