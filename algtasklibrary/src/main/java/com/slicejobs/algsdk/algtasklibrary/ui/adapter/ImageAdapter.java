package com.slicejobs.algsdk.algtasklibrary.ui.adapter;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
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

                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(false)//这只图片不缓存在内存中，避免内存泄漏
                        .cacheOnDisk(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .showImageOnFail(R.drawable.ic_photo_default_show)
                        .showImageOnLoading(R.drawable.ic_photo_default_show)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)    //设置图片的缩放类型，该方法可以有效减少内存的占用
                        .build();

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
                                    int DrawableId = field.getInt(field.getName());
                                    Glide.with(WXEnvironment.getApplication()).load(DrawableId).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else {
                        Glide.with(WXEnvironment.getApplication()).load(temp).asGif().into(view);
                    }
                } else {
                    //Glide.with(WXEnvironment.getApplication()).load(temp).asBitmap().into(new WeeXImageTarget(strategy, temp, view));
                    ImageLoader.getInstance().displayImage(temp, view, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = "下载错误";
                                    break;
                                case DECODING_ERROR:
                                    message = "图片无法显示";
                                    break;
                                case NETWORK_DENIED:
                                    message = "网络有问题，无法下载";
                                    break;
                                case OUT_OF_MEMORY:
                                    message = "图片太大无法显示";
                                    break;
                                case UNKNOWN:
                                    message = "未知的错误";
                                    break;
                            }

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                        }
                    });
                }

            }
        }, 0);

    }
}