package com.slicejobs.algsdk.algtasklibrary.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.TaskImage;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.imagelook.TaskImageDetailFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.imagelook.ViewPagerFixed;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class BigImageSelectActivity extends BaseActivity {

    @BindView(R2.id.imageViewPager)
    ViewPagerFixed viewPage;
    @BindView(R2.id.img_checkbox)
    CheckBox imgCheck;
    @BindView(R2.id.title_layout)
    FrameLayout titleLayout;
    @BindView(R2.id.operate_layout)
    FrameLayout operateLayout;
    @BindView(R2.id.action_comfirm)
    TextView selectConfirm;
    private ArrayList<String> urls = null;
    private ArrayList<TaskImage> taskImages = new ArrayList<TaskImage>();
    private ArrayList<String> selectUrls;
    private int position = 1;
    private boolean ifPhotoFullScreen;

    //添加多张照片
    public static Intent getIntent(Context context, ArrayList<String> urls, int position, ArrayList<String> selectUrls) {
        Intent intent = new Intent(context, BigImageSelectActivity.class);
        intent.putStringArrayListExtra("urls", urls);
        intent.putExtra("position", position);
        intent.putExtra("selectUrls", selectUrls);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_big_image);
        ButterKnife.bind(this);
        urls = getIntent().getStringArrayListExtra("urls");
        selectUrls = getIntent().getStringArrayListExtra("selectUrls");
        position = getIntent().getIntExtra("position", 0);
        if (selectUrls != null) {
            if (selectUrls.size() != 0) {
                selectConfirm.setEnabled(true);
                selectConfirm.setBackgroundResource(R.drawable.selector_button_big_corners);
            } else {
                selectConfirm.setEnabled(false);
                selectConfirm.setBackgroundResource(R.drawable.shape_button_diaable_big_corners);
            }
        }
        initView();

    }

    private void initView() {
        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
        viewPage.setAdapter(imagePagerAdapter);
        viewPage.setCurrentItem(position);
        if (urls != null && selectUrls != null) {
            String selectUrl = urls.get(position);
            if(selectUrls.contains(selectUrl)){
                imgCheck.setChecked(true);
            } else {
                imgCheck.setChecked(false);
            }
        }
        // 更新下标
        viewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int arg0) {
                position = arg0;
                if (urls != null && selectUrls != null) {
                    String selectUrl = urls.get(position);
                    if(selectUrls.contains(selectUrl)){
                        imgCheck.setChecked(true);
                    } else {
                        imgCheck.setChecked(false);
                    }
                }
            }
        });
        imgCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (urls != null && selectUrls != null) {
                    String selectUrl = urls.get(position);
                    if(isChecked){
                        if(!selectUrls.contains(selectUrl)){
                            selectUrls.add(selectUrl);
                        }
                    } else {
                        if(selectUrls.contains(selectUrl)){
                            selectUrls.remove(selectUrl);
                        }
                    }
                    if (selectUrls.size() != 0) {
                        selectConfirm.setEnabled(true);
                        selectConfirm.setBackgroundResource(R.drawable.selector_button_big_corners);
                    } else {
                        selectConfirm.setEnabled(false);
                        selectConfirm.setBackgroundResource(R.drawable.shape_button_diaable_big_corners);
                    }
                }
            }
        });
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ArrayList<String> fileList;

        public ImagePagerAdapter(FragmentManager fm, ArrayList<String> fileList) {
            super(fm);
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList == null ? 0 : fileList.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = fileList.get(position);
            if(url.startsWith("/")){
                url ="file://" + url;
            }
            TaskImageDetailFragment taskImageDetailFragment = TaskImageDetailFragment.newInstance(url);
            taskImageDetailFragment.setPhotoClickListener(new TaskImageDetailFragment.PhotoClickListener() {
                @Override
                public void onPhotoClick() {
                    ifPhotoFullScreen = !ifPhotoFullScreen;
                    if (ifPhotoFullScreen) {
                        titleLayout.setVisibility(View.GONE);
                        operateLayout.setVisibility(View.GONE);
                    } else {
                        titleLayout.setVisibility(View.VISIBLE);
                        operateLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            return taskImageDetailFragment;
        }
    }


    @OnClick({R2.id.action_return,R2.id.action_comfirm})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            this.finish();
        } else if (view.getId() == R.id.action_comfirm) {
            Intent intentConfirm = new Intent();
            intentConfirm.putExtra("selectUrls",selectUrls);
            setResult(RESULT_OK,intentConfirm);
            this.finish();
        }
    }

}
