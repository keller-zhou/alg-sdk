package com.slicejobs.algsdk.algtasklibrary.ui.adapter;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.adapter.IWXImgLoaderAdapter;
import com.taobao.weex.common.WXImageStrategy;
import com.taobao.weex.dom.WXImageQuality;

import java.lang.reflect.Field;

public class ImageAdapter implements IWXImgLoaderAdapter {


    @Override
    public void setImage(String url, ImageView view, WXImageQuality quality, WXImageStrategy strategy) {
        //Picasso默认会使用设备的15%的内存作为内存图片缓存，且现有的api无法清空内存缓存。我们可以在查看大图时放弃使用内存缓存，图片从网络下载完成后会缓存到磁盘中，加载会从磁盘中加载，这样可以加速内存的回收。
//       if (StringUtil.isNotBlank(url)) {
//           Picasso.with(view.getContext()).load(url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(view);
//       }

        //使用 理论可以更好控制示例照内存，测试发现6.0以上系统，图片加载不出来，6.0以下正常
        //ImageLoader.getInstance().displayImage(url,view);

        WXSDKManager.getInstance().postOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (view == null || view.getLayoutParams() == null) {
                    return;
                }
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                if (url.startsWith("file://")) {
                    Glide.with(WXEnvironment.getApplication()).load(url).into(view);
                    return;
                }
                String temp = url;
                if (url.startsWith("//")) {
                    temp = "http:" + url;
                }else if (url.startsWith("/public")) {
                    temp = AppConfig.webHost.getAppWebHost() + url;
                }


                if (url.endsWith(".gif")) {
                    if (url.contains("/large-image/")) {
                        String[] urlStrArray = url.split("/");
                        if (urlStrArray != null && urlStrArray.length !=0) {
                            String gifNameStr = urlStrArray[urlStrArray.length - 1];
                            if (StringUtil.isNotBlank(gifNameStr) && gifNameStr.length() > 4) {
                                gifNameStr = gifNameStr.substring(0, gifNameStr.length() - 4);
                                Field field = null;
                                try {
                                    field = R.drawable.class.getField(gifNameStr);
                                    int DrawableId = field.getInt(new R.drawable());
                                    Glide.with(WXEnvironment.getApplication()).load(DrawableId).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else {
                        Glide.with(WXEnvironment.getApplication()).load(url).asGif().into(view);
                    }
                } else {
                    Glide.with(WXEnvironment.getApplication()).load(temp).asBitmap().into(new WeeXImageTarget(strategy, url, view));
                }

            }
        }, 0);

    }

    private class WeeXImageTarget extends SimpleTarget<Bitmap> {

        private WXImageStrategy mWXImageStrategy;
        private String mUrl;
        private ImageView mImageView;

        WeeXImageTarget(WXImageStrategy strategy, String url, ImageView imageView) {
            mWXImageStrategy = strategy;
            mUrl = url;
            mImageView = imageView;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            mImageView.setImageBitmap(resource);
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
    }
}