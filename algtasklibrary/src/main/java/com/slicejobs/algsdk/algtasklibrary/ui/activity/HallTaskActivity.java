package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.HallFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by keller.zhou on 2015/11/30.
 */
public class HallTaskActivity extends BaseActivity {
    HallFragment hallFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halltask);
        ButterKnife.bind(this);
        //加载门店任务fragment
        hallFragment = HallFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, hallFragment)
                .commit();
        
    }

    @OnClick({R2.id.action_go_back,R2.id.action_go_watch_store})
    public void OnClick(View viwe) {
        if (viwe.getId() == R.id.action_go_back) {
            this.finish();
        } else if (viwe.getId() == R.id.action_go_watch_store) {
            Intent intent = new Intent(this,AddMarketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
            startActivity(intent);
        }
    }


}
