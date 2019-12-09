package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.taobao.weex.WXSDKInstance;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by keller.zhou on 17/2/10.
 */
public class WeexPublicActivity extends BaseActivity implements IJsRenderListener {
    @BindView(R2.id.weex_view_layout)
    RelativeLayout weexLayout;
    @BindView(R2.id.weex_view_title)
    TextView tvWeexTitle;
    WXSDKInstance mWXSDKInstance;
    private String jsUrl;
    private String initJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weex_public);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        SerializableBaseMap serializableBaseMap = (SerializableBaseMap) bundle.get("weex_data");

        Map<String, Object> data = serializableBaseMap.getMap();
        if (StringUtil.isNotBlank(data.get("title").toString())) {
            tvWeexTitle.setText(data.get("title").toString());
        }

        if (null != data.get("initData") && StringUtil.isNotBlank(data.get("initData").toString())) {
            initJson = data.get("initData").toString();
        }

        jsUrl = data.get("jsUrl").toString();
        loadingJsUrl();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        weexLayout.addView(view);
    }

    @OnClick({R2.id.action_return})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            finish();
        }
    }

    private void loadingJsUrl() {
        weexLayout.setVisibility(View.VISIBLE);
        if (StringUtil.isNotBlank(jsUrl)) {
            String[] subJsurlArray = jsUrl.split("/");
            renderJs(subJsurlArray[subJsurlArray.length - 1],initJson,"WeexPublic",this);
        }
    }

}
