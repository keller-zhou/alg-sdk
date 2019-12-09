package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by nlmartian on 9/23/15.
 */
public class MyLoginViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private List<String> mTitleList;

    public MyLoginViewPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> tilteLis) {
        super(fm);
        mFragmentList = fragmentList;
        mTitleList = tilteLis;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    //获取标题
    @Override
    public CharSequence getPageTitle(int position) {

        return mTitleList.get(position);
    }
}
