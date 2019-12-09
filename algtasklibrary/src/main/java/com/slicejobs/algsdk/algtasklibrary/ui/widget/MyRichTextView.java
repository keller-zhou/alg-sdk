package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;

public class MyRichTextView extends FrameLayout {

    private WebView webview;

    public MyRichTextView(Context context) {
        super(context);
        initView(context);
    }

    public MyRichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MyRichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyRichTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initView(Context context){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.rich_text_view, this);
        webview = (WebView) findViewById(R.id.webview);
    }

    public WebView getWebview() {
        return webview;
    }
}
