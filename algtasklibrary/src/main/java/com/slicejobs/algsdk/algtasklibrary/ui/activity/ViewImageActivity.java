package com.slicejobs.algsdk.algtasklibrary.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.imagelook.ImageDetailFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.imagelook.ViewPagerFixed;
import com.slicejobs.algsdk.algtasklibrary.utils.AndroidUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.FileUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.imagedecode.ImageDecodeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 *
 */
public class ViewImageActivity extends BaseActivity {

    @BindView(R2.id.imageViewPager)
    ViewPagerFixed viewPage;
    @BindView(R2.id.tv_hint_currphoto_page)
    TextView tvHintPosition;
    @BindView(R2.id.tv_previous)
    TextView tvPrevious;
    @BindView(R2.id.tv_next)
    TextView tvNext;
    @BindView(R2.id.photo_delete)
    LinearLayout photo_delete;
    @BindView(R2.id.rootView)
    FrameLayout rootView;
    @BindView(R2.id.decode_barcode)
    LinearLayout decodeBarcodeLayout;
    private ArrayList<String> urls = null;
    private int position = 1;
    private boolean ifBrowserFinished;

    private int decodePhotoMsgWhat = 0x1001;
    private boolean isIfBrowserFinished;
    private boolean ifCanDelete;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == decodePhotoMsgWhat) {
                if (StringUtil.isNotBlank(msg.obj.toString()) && msg.obj.toString().contains("http://weixin.qq.com")) {
                    toast("这是微信的外部连接，无法在打开！");
                } else if (StringUtil.isNotBlank(msg.obj.toString()) && msg.obj.toString().contains("http://")) {//跳转网页
                    startActivity(WebviewActivity.getStartIntent(ViewImageActivity.this, msg.obj.toString()));
                } else if (StringUtil.isNotBlank(msg.obj.toString())){
                    toast("识别结果："+msg.obj.toString());
                }
            }
            super.handleMessage(msg);
        }
    };


    //添加多张照片
    public static Intent getIntent(Context context, ArrayList<String> urls, int position) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putStringArrayListExtra("urls", urls);
        intent.putExtra("position", position);
        return intent;
    }

    //添加多张照片
    public static Intent getIntent(Context context, ArrayList<String> urls, int position, boolean browserCache, boolean ifCanDelete) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putStringArrayListExtra("urls", urls);
        intent.putExtra("position", position);
        intent.putExtra("ifBrowserCache", browserCache);
        intent.putExtra("ifCanDelete", ifCanDelete);
        return intent;
    }

    //查看一张照片
    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("url", url);
        return intent;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ButterKnife.bind(this);
        urls = getIntent().getStringArrayListExtra("urls");
        if (null == urls) {
            String url = getIntent().getStringExtra("url").toString().trim();
            urls = new ArrayList<>();
            urls.add(url);
        }

        position = getIntent().getIntExtra("position", 0);
        isIfBrowserFinished = getIntent().getBooleanExtra("ifBrowserCache", false);
        ifCanDelete = getIntent().getBooleanExtra("ifCanDelete",true);
        if(isIfBrowserFinished && ifCanDelete){
            photo_delete.setVisibility(View.VISIBLE);
        }
        initView();

    }

    private void initView() {
        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
        viewPage.setAdapter(imagePagerAdapter);
        viewPage.setCurrentItem(position);
        //初始化下标
        showBottomHint();

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
                if(urls != null && urls.size() != 0){
                    if(position == urls.size() - 1){
                        ifBrowserFinished = true;
                    }
                }
                showBottomHint();
            }
        });
    }

    private void showBottomHint() {
        if (viewPage.getAdapter().getCount() == 1) {//只有一张照片
            tvPrevious.setVisibility(View.INVISIBLE);
            tvNext.setVisibility(View.INVISIBLE);
        } else if (position == 0) {
            tvPrevious.setVisibility(View.INVISIBLE);
            tvNext.setVisibility(View.VISIBLE);
        } else if (viewPage.getAdapter().getCount() == position+1) {
            tvPrevious.setVisibility(View.VISIBLE);
            tvNext.setVisibility(View.INVISIBLE);
        } else {
            tvPrevious.setVisibility(View.VISIBLE);
            tvNext.setVisibility(View.VISIBLE);
        }
        tvHintPosition.setText(position + 1 + "/" + viewPage.getAdapter().getCount());

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
            return ImageDetailFragment.newInstance(url);
        }
    }


    @OnClick({R2.id.tv_previous, R2.id.action_return, R2.id.tv_next, R2.id.action_more,R2.id.photo_delete,R2.id.decode_barcode,R2.id.rootView})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            Intent intent = new Intent();
            intent.putExtra("ifBrowserFinished",ifBrowserFinished);
            intent.putExtra("urls",urls);
            setResult(RESULT_OK,intent);
            this.finish();
        } else if (view.getId() == R.id.tv_previous) {
            viewPage.setCurrentItem(-- position);
        } else if (view.getId() == R.id.tv_next) {
            viewPage.setCurrentItem(++ position);
        } else if (view.getId() == R.id.action_more) {
            rootView.setVisibility(View.VISIBLE);
            ScaleAnimation zoomInSa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.8f, Animation.RELATIVE_TO_SELF, 0);
            zoomInSa.setDuration(300);
            zoomInSa.setFillAfter(true);
            decodeBarcodeLayout.startAnimation(zoomInSa);
        } else if (view.getId() == R.id.photo_delete) {
            if(urls != null && urls.size() != 0){
                urls.remove(position);
                viewPage.getAdapter().notifyDataSetChanged();
                if(urls.size() >0){
                    if(position != 0){
                        viewPage.setCurrentItem(-- position);
                    }else{
                        Intent backIntent = new Intent();
                        backIntent.putExtra("ifBrowserFinished",ifBrowserFinished);
                        backIntent.putExtra("urls",urls);
                        setResult(RESULT_OK,backIntent);
                        this.finish();
                    }
                }else{
                    Intent backIntent = new Intent();
                    backIntent.putExtra("ifBrowserFinished",ifBrowserFinished);
                    backIntent.putExtra("urls",urls);
                    setResult(RESULT_OK,backIntent);
                    this.finish();
                }
            }else {

            }
        } else if (view.getId() == R.id.decode_barcode) {
            decode();
        } else if (view.getId() == R.id.rootView) {
            ScaleAnimation zoomOutSa = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.8f, Animation.RELATIVE_TO_SELF, 0);
            zoomOutSa.setDuration(300);
            zoomOutSa.setFillAfter(true);
            zoomOutSa.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rootView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            decodeBarcodeLayout.startAnimation(zoomOutSa);
        }
    }

    /**
     * 监听手机返回键
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra("ifBrowserFinished",ifBrowserFinished);
            intent.putExtra("urls",urls);
            setResult(RESULT_OK,intent);
            this.finish();
            return true;
        }else{

        }
        return super.onKeyDown(keyCode, event);
    }

    private void savePhoto() {
        if (null != urls) {
            String saveUrl = urls.get(position);
            if (StringUtil.isBlank(saveUrl)) {
                return;
            }
            ImageLoader imageLoader = ImageLoader.getInstance();
            File cacheFile = DiskCacheUtils.findInCache(saveUrl, imageLoader.getDiskCache());

            File sdcard = Environment.getExternalStorageDirectory();
            String distPath = sdcard.getAbsolutePath() + "/" + AppConfig.APP_FOLDER_NAME + "/" + System.currentTimeMillis() + ".jpg";
            if (cacheFile != null && cacheFile.exists()) {
                Observable.create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        try {
                            FileUtil.copyFile(cacheFile.getAbsolutePath(), distPath);
                            AndroidUtil.updateSystemGallery(distPath);
                            subscriber.onNext(null);
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object r) {
                                ViewImageActivity.this.toast(getString(R.string.save_success));
                            }
                        }, e -> toast(getString(R.string.save_failed)));
            } else {
                toast(getString(R.string.save_failed));
            }
        }
    }

    private void decode() {
        if (null != urls) {
            String saveUrl = urls.get(position);
            if (StringUtil.isBlank(saveUrl)) {
                return;
            }
            ImageLoader imageLoader = ImageLoader.getInstance();
            Bitmap bitmap;
            File cacheFile = DiskCacheUtils.findInCache(saveUrl, imageLoader.getDiskCache());
            if(cacheFile == null){
                bitmap = BitmapFactory.decodeFile(saveUrl);
            }else {
                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            }
            if (null == bitmap) {
                return;
            }

            Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    Result result = ImageDecodeUtil.parseQRcodeBitmap(bitmap);
                    if (null != result) {
                        Message msg = Message.obtain();
                        msg.what = decodePhotoMsgWhat;
                        msg.obj = result.toString();
                        handler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = decodePhotoMsgWhat;
                        msg.obj = "这不是二维码或识别失败！";
                        handler.sendMessage(msg);
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

}
