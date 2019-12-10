package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ActionSheetDialog;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.ProgressWebView;
import com.slicejobs.algsdk.algtasklibrary.utils.AndroidUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.imagedecode.ImageDecodeUtil;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class WebviewActivity extends BaseActivity {

    private int DECODE_QRCODE_FAIL = 0x1001;
    private int DECODE_QRCODE_SUCCESS = 0x1002;
    @BindView(R2.id.webview)
    ProgressWebView webView;
    @BindView(R2.id.title)
    TextView title;
    public String url = null;
    private String titleStr;
    private String pageTag;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                if (webView != null ) {
                    webView.destroy();
                }
            }else if(msg.what == DECODE_QRCODE_FAIL){
                toast("这不是二维码或识别失败");
            }else if(msg.what == DECODE_QRCODE_SUCCESS){
                if (StringUtil.isNotBlank(msg.obj.toString()) && msg.obj.toString().contains("http")) {//跳转网页
                    //startActivity(WebviewActivity.getStartIntent(WebviewActivity.this, msg.obj.toString()));
                    webView.resetWebviewUI();
                    initWebview(msg.obj.toString());
                } else if (StringUtil.isNotBlank(msg.obj.toString())){
                    toast("识别结果："+msg.obj.toString());
                }
            }else {

            }
            super.handleMessage(msg);
        }
    };



    public static Intent getStartIntent(Context context, String url) {//启动一个简单的浏览器
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("url", url);
        return intent;
    }

    public static Intent getStartIntentCustom(Context context, String url, Map<String,Object> params) {//启动一个自定义
        Intent intent = new Intent(context, WebviewActivity.class);
        Bundle bundle = new Bundle();
        SerializableBaseMap tmpmap=new SerializableBaseMap();
        tmpmap.setMap(params);
        bundle.putSerializable("params", tmpmap);
        intent.putExtras(bundle);
        intent.putExtra("url", url);
        return intent;
    }

    public static Intent getStartIntentCustomJavaScript(Context context, String url, String useJs, String jsText) {//启动一个自定义(可js注入)
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("useJs", useJs);
        intent.putExtra("jsText", jsText);
        intent.putExtra("url", url);
        return intent;
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        url = getIntent().getStringExtra("url");

        if (!StringUtil.isNotBlank(url)) {//空的
            finish();
        }
        initCostom();

        initWebview(url);

    }


    private void initCostom() {

        Bundle bundle = getIntent().getExtras();
        if (bundle.get("params") == null) {
            return ;
        }

        SerializableBaseMap serializableBaseMap = (SerializableBaseMap) bundle.get("params");
        Map<String, Object> params = serializableBaseMap.getMap();
        Map<String, Object> data = (Map<String, Object>) params.get("data");

        if (null != data) {
            if (null != data.get("title") && StringUtil.isNotBlank(data.get("title").toString()) && !data.get("title").toString().startsWith("http")) {
                titleStr = data.get("title").toString();
            }

            if (null != data.get("pageTag") && StringUtil.isNotBlank(data.get("pageTag").toString())) {
                pageTag = data.get("pageTag").toString();
            }
        }

    }



    @SuppressWarnings("NewApi")
    private void initWebview(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url1));
            startActivity(intent);
        });


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // 获取上下文, H5PayDemoActivity为当前页面
                final Activity context = WebviewActivity.this;
                // ------  对alipays:相关的scheme处理 -------
                if (url.startsWith("alipays:") || url.startsWith("alipay")) {
                    try {
                        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    } catch (Exception e) {
                        new AlertDialog.Builder(context)
                                .setMessage("未检测到支付宝客户端，请安装后重试。")
                                .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                        context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                    }
                                }).setNegativeButton("取消", null).show();
                    }
                    return true;
                } else if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("tbopen://")) {//淘宝
                    if (AndroidUtil.isAppInstalled(WebviewActivity.this, "com.taobao.taobao")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(context)
                                .setMessage("未检测到淘宝客户端，请安装后重试。")
                                .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri alipayUrl = Uri.parse("https://mpage.taobao.com/hd/download.html");
                                        context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                    }
                                }).setNegativeButton("取消", null).show();
                    }
                    return true;
                } else {

                }

                // ------- 其他应用协议，爱零工不给予处理 -------

                if (!(url.startsWith("http") || url.startsWith("https"))) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (StringUtil.isNotBlank(titleStr)) {
                    title.setText(titleStr);
                } else {
                    if(!StringUtil.isContainEnglish(view.getTitle())) {
                        title.setText(view.getTitle());
                    }
                }

                webView.setOnLongClickListener(onLongClickListener);

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                addNetErrorView(new OnNetErrorRefreshClick() {
                    @Override
                    public void onRefreshClick() {
                        if (webView != null && StringUtil.isNotBlank(url)) {
                            webView.loadUrl(url);
                        }
                    }
                });
            }

        });

        webView.addJavascriptInterface(new JSIntface(), "JSIntface");
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.loadUrl(url);
    }






    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    public String getCookie (String url) {
        CookieManager cookiemanager = CookieManager.getInstance();

        return cookiemanager.getCookie(url);

    }

    @OnClick({R2.id.action_go_back})
    public void onClick(View view) {
        if (view.getId() == R.id.action_go_back) {
            if (webView.canGoBack()) {//当前层返回
                webView.goBack();
            } else {
                WebviewActivity.this.finish();//关闭activity层
            }
        }
    }

    class JSIntface{


        /**
         * 这个方法提供给js，打开游戏入口(note备注)
         */
        @JavascriptInterface
        public void openWebview(String gameUrl, String useJs, String jsText) {
            Intent intent = WebviewActivity.getStartIntentCustomJavaScript(WebviewActivity.this, gameUrl, useJs, jsText);
            startActivity(intent);
        }

        /**
         * 这个方法提供给js，打开小程序
         */
        @JavascriptInterface
        public void openMiniprogram(String miniprogramSouceId, int miniprogramType, String miniprogramPath) {
            IWXAPI api = WXAPIFactory.createWXAPI(WebviewActivity.this, AppConfig.WX_APP_ID);//填应用AppId
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = miniprogramSouceId; // 填小程序原始id
            req.miniprogramType = miniprogramType;// 可选打开 开发版，体验版和正式版
            req.path = miniprogramPath;
            api.sendReq(req);
        }

        /**
         * 这个方法提供给js，打开小程序
         */
        @JavascriptInterface
        public void closeAndRefresh() {
            setResult(RESULT_OK);
            finish();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        /**
         * WebView中包含一个ZoomButtonsController，当使用web.getSettings().setBuiltInZoomControls(true);启用后，用户一旦触摸屏幕，就会出现缩放控制图标。如果图标自动消失前退出当前Activity的话
         */
        if(webView != null) {
            webView.getSettings().setBuiltInZoomControls(true);
            webView.setVisibility(View.GONE);
            long timeout = ViewConfiguration.getZoomControlsTimeout();//timeout ==3000




            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    handler.sendEmptyMessageAtTime(1, timeout);


                }
            }, timeout);
        }

        super.onDestroy();
    }


    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            WebView.HitTestResult hitTestResult = webView.getHitTestResult();
            // 如果是图片类型或者是带有图片链接的类型
            if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                    hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                new ActionSheetDialog(WebviewActivity.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem("识别图中二维码", ActionSheetDialog.SheetItemColor.Black,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {

                                        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
                                            @Override
                                            public void call(Subscriber<? super Object> subscriber) {
                                                //找到当前页面的根布局
                                                View rootView = getWindow().getDecorView().getRootView();
                                                rootView.setDrawingCacheEnabled(true);
                                                //从缓存中获取当前屏幕的图片,创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
                                                rootView.buildDrawingCache();
                                                Bitmap tempBitmap = rootView.getDrawingCache();
                                                if (null == tempBitmap) {
                                                    Message msg = Message.obtain();
                                                    msg.what = DECODE_QRCODE_FAIL;
                                                    handler.sendMessage(msg);
                                                    return;
                                                }

                                                Result result = ImageDecodeUtil.parseQRcodeBitmap(tempBitmap);
                                                if (null != result) {
                                                    Message msg = Message.obtain();
                                                    msg.what = DECODE_QRCODE_SUCCESS;
                                                    msg.obj = result.toString();
                                                    handler.sendMessage(msg);
                                                } else {
                                                    Message msg = Message.obtain();
                                                    msg.what = DECODE_QRCODE_FAIL;
                                                    handler.sendMessage(msg);
                                                }
                                                rootView.setDrawingCacheEnabled(false);
                                            }
                                        }).subscribeOn(Schedulers.io())
                                                .subscribe();
                                    }
                                }).show();
                return true;
            }
            return false;//保持长按可以复制文字
        }
    };

}
