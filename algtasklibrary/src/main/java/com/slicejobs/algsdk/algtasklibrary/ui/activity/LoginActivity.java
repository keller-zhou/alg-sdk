package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.MyLoginViewPagerAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.AccountLoginFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.fragment.ShortMsgLoginFragment;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by nlmartian on 7/8/15.
 */
public class LoginActivity extends BaseActivity {

    @BindView(R2.id.tabs)
    TabLayout tabs;
    @BindView(R2.id.vp_view)
    ViewPager vp_view;
    private List<String> mTitleList = new ArrayList<String>();//页卡标题集合
    private AccountLoginFragment accountView;
    private ShortMsgLoginFragment messageView;//页卡视图
    private List<Fragment> mViewList = new ArrayList<>();//页卡视图集合
    MyLoginViewPagerAdapter mAdapter;
    IntentFilter intentFilter;
    private String inputPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        accountView = new AccountLoginFragment();
        messageView = new ShortMsgLoginFragment();

        //下划线与内容同宽
        tabs.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //拿到tabLayout的mTabStrip属性
                    Field mTabStripField = tabs.getClass().getDeclaredField("mTabStrip");
                    mTabStripField.setAccessible(true);
                    LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(tabs);
                    int dp25 = DensityUtil.dip2px(tabs.getContext(), 25);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabView = mTabStrip.getChildAt(i);
                        //拿到tabView的mTextView属性
                        Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(tabView);
                        tabView.setPadding(0, 0, 0, 0);
                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                        int width = 0;
                        width = mTextView.getWidth();
                        if (width == 0) {
                            mTextView.measure(0, 0);
                            width = mTextView.getMeasuredWidth();
                        }
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                        params.width = width ;
                        params.leftMargin = dp25;
                        params.rightMargin = dp25;
                        tabView.setLayoutParams(params);
                        tabView.invalidate();
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        mTitleList.add("账号登录");
        mTitleList.add("短信登录");
        mViewList.add(accountView);
        mViewList.add(messageView);
        //添加tab选项卡，默认第一个选中
        tabs.addTab(tabs.newTab().setText(mTitleList.get(0)), true);
        tabs.addTab(tabs.newTab().setText(mTitleList.get(1)));

        mAdapter = new MyLoginViewPagerAdapter(getSupportFragmentManager(),mViewList,mTitleList);
        //给ViewPager设置适配器
        vp_view.setAdapter(mAdapter);

        //将TabLayout和ViewPager关联起来
        tabs.setupWithViewPager(vp_view);
        //给Tabs设置适配器
        tabs.setTabsFromPagerAdapter(mAdapter);
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (StringUtil.isNotBlank(inputPhoneNumber)) {
                    int selectedPosition = tab.getPosition();
                    if (selectedPosition == 0) {
                        accountView.setInputPhoneNumber(inputPhoneNumber);
                    } else {
                        messageView.setInputPhoneNumber(inputPhoneNumber);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int unSelectedPosition = tab.getPosition();
                if (unSelectedPosition == 0) {
                    inputPhoneNumber = accountView.getInputPhoneNumber();
                } else {
                    inputPhoneNumber = messageView.getInputPhoneNumber();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}
