package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.slicejobs.algsdk.algtasklibrary.ui.activity.WebviewActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.MyRichTextView;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.Component;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by keller.zhou on 17/1/22.
 * 为h5提供的自定义富文本插件
 * h5使用方法:
 *<rich-text style="height: 200px; width: 750px;" content='<p><b>加粗，</b>aaaaaa<a href=\"http://baidu.com\" target=\"_blank\" style=\"background-color: rgb(255, 255, 255);\">百度</a><font color=\"#00ff00\">&nbsp;绿色</font></p>'></rich_text>
 */
@Component(lazyload = true)
public class WebViewRichTitleComponent extends WXComponent<MyRichTextView> {
    WXSDKInstance mInstance;
    private String content;
    private WebView webView;

    public WebViewRichTitleComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mInstance = instance;
    }

    @Override
    protected MyRichTextView initComponentHostView(Context context) {
        MyRichTextView richTextView = new MyRichTextView(context);
        webView = richTextView.getWebview();
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(false);

                return false;
            }
        });
        webView.loadDataWithBaseURL(null, "<head></head>", "text/html", "utf-8", null);
        return richTextView;
    }

    @WXComponentProp(name = "content")
    public void showRichTextContent(String content) {
        this.content = content;
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:auto; height:auto;}</style>" +
                "</head>";
        String htmlStr = "<html>" + head + "<body>" + content + "</body></html>";
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.addJavascriptInterface(new WebViewResizer(), "WebViewResizer");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.WebViewResizer.processHeight(document.querySelector('body').offsetHeight);");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // ------  对alipays:相关的scheme处理 -------
                if (url.startsWith("slicejobs://")) {
                    Uri uri = Uri.parse(url);
                    String action = uri.getQueryParameter("action");
                    String openid = uri.getQueryParameter("openid");

                    return true;
                }
                // ------- 处理结束 -------
                if (!(url.startsWith("http") || url.startsWith("https"))) {
                    return true;
                }


                mInstance.getContext().startActivity(WebviewActivity.getStartIntent(mInstance.getContext(), url));
                return true;
            }
        });
        // 加载并显示HTML代码
        webView.loadDataWithBaseURL(null, htmlStr, "text/html", "utf-8", null);
    }

    private class WebViewResizer {
        @JavascriptInterface
        public void processHeight(String height) {
            // height is in DP units. Convert it to PX if you are adjusting the WebView's height.
            // height could be 0 if WebView visibility is Visibility.GONE.
            // If changing the WebView height, do it on the main thread!
            int h = DensityUtil.dip2px(mInstance.getContext(), Float.parseFloat(height));

            Map<String, Object> params = new HashMap<>();
            params.put("titleHeight", h);
            //发送全局监听
            mInstance.fireGlobalEventCallback("richComponentEvent", params);
        }
    }
}
