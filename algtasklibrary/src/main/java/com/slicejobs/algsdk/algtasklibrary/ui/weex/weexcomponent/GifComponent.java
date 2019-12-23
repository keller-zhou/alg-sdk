package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.Component;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.lang.reflect.Field;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by keller.zhou on 17/1/22.
 * 为h5提供的自定义富文本插件
 * h5使用方法:
 *<rich-text style="height: 200px; width: 750px;" content='<p><b>加粗，</b>aaaaaa<a href=\"http://baidu.com\" target=\"_blank\" style=\"background-color: rgb(255, 255, 255);\">百度</a><font color=\"#00ff00\">&nbsp;绿色</font></p>'></rich_text>
 */
@Component(lazyload = true)
public class GifComponent extends WXComponent<GifImageView> {
    WXSDKInstance mInstance;
    private GifImageView gifImageView;

    public GifComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mInstance = instance;
    }


    @Override
    protected GifImageView initComponentHostView(Context context) {
        gifImageView = new GifImageView(context);

        return gifImageView;
    }


    @WXComponentProp(name = "source")
    public void showGif(String url) {
        if (url.contains("/large-image/")) {
            String[] urlStrArray = url.split("/");
            if (urlStrArray != null && urlStrArray.length !=0) {
                String gifNameStr = urlStrArray[urlStrArray.length - 1];
                if (StringUtil.isNotBlank(gifNameStr) && gifNameStr.length() > 4) {
                    gifNameStr = gifNameStr.substring(0, gifNameStr.length() - 4);
                    Field field = null;
                    try {
                        field = R.drawable.class.getField(gifNameStr);
                        int drawableId = field.getInt(field.getName());
                        gifImageView.setBackgroundResource(drawableId);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            Glide.with(WXEnvironment.getApplication()).load(url).into(gifImageView);
        }
    }

}
