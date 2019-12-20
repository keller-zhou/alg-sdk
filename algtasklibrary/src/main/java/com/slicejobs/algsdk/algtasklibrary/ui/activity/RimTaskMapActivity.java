package com.slicejobs.algsdk.algtasklibrary.ui.activity;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;
import java.util.HashMap;
import java.util.Map;
import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;


public class RimTaskMapActivity extends BaseActivity implements IJsRenderListener {

    @BindView(R2.id.neartask_web_layout)
    RelativeLayout nearTaskWebLayout;
    @BindView(R2.id.action_search_task)
    ImageView actionSearch;
    @BindView(R2.id.action_store_task)
    ImageView actionStoreTask;
    @BindView(R2.id.tasks_search_layout)
    FrameLayout taskSearchView;
    @BindView(R2.id.et_search_task)
    EditText searchTask;
    @BindView(R2.id.cacel_search)
    TextView cacelSearch;

    private  WXSDKInstance mWXSDKInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        setContentView(R.layout.activity_rimtask_map);
        ButterKnife.bind(this);
        init();

    }



    public void init() {
        renderJs(AppConfig.TASK_LIST_NEARBY_VIEW_FILE,null,"周边赚",this);

        //搜索框
        searchTask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = searchTask.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > searchTask.getWidth()
                        - searchTask.getPaddingRight()
                        - drawable.getIntrinsicWidth()){
                    searchTask.setText("");
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
                Drawable seach = res.getDrawable(R.drawable.ic_search_image);
                seach.setBounds(0, 0, seach.getMinimumWidth(), seach.getMinimumHeight());
                if(StringUtil.isBlank(searchTask.getText().toString())) {
                    searchTask.setCompoundDrawables(seach,null,null,null);
                }else{
                    searchTask.setCompoundDrawables(seach,null,close,null);
                }
                keywordSearchTask(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @OnClick({R2.id.action_return,R2.id.action_store_task,R2.id.action_search_task,R2.id.cacel_search})
    public void OnClick(View view) {
        if (view.getId() == R.id.action_return) {
            this.finish();
        } else if (view.getId() == R.id.action_store_task) {
            Intent intent = new Intent(this, HallTaskActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.action_search_task) {
            actionSearch.setVisibility(View.GONE);
            actionStoreTask.setVisibility(View.GONE);
            taskSearchView.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.cacel_search) {
            actionSearch.setVisibility(View.VISIBLE);
            actionStoreTask.setVisibility(View.VISIBLE);
            taskSearchView.setVisibility(View.GONE);
        }
    }



    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        nearTaskWebLayout.addView(view);
    }

    @Subscribe
    public void onRefreshTaskEvent(AppEvent.RefreshTaskEvent event) {
        if (StringUtil.isNotBlank(event.status) && event.status.equals("nearby_task_list")) {
            Map<String, Object> params = new HashMap<>();
            params.put("updateType", "refreshTask");
            if (null != mWXSDKInstance) {
                mWXSDKInstance.fireGlobalEventCallback("nearbyTaskListChange", params);
            }
        }
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    //职位关键字搜索
    public void keywordSearchTask(String keyWord) {
        if (null != mWXSDKInstance) {
            Map<String, Object> params = new HashMap<>();
            params.put("updateType", "keywordSearch");//刷新数据
            params.put("keyword", keyWord);
            mWXSDKInstance.fireGlobalEventCallback("nearbyTaskListChange", params);
        }
    }
}
