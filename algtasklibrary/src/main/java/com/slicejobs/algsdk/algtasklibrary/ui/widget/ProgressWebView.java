package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;


/**
 * Created by keller.zhou on 17/4/20.
 */

@SuppressWarnings("deprecation")
public class ProgressWebView extends WebView {

    private ProgressBar progressbar;
    private int curProgress;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                if(curProgress < 90) {
                    curProgress += 1;
                    progressbar.setProgress(curProgress);
                    handler.sendEmptyMessageDelayed(0, 1);
                }
            }else if(msg.what == 1){
                curProgress += 1;
                progressbar.setProgress(curProgress);
                handler.sendEmptyMessageDelayed(1, 1);
                if (curProgress == 100) {
                    handler.sendEmptyMessageDelayed(2, 100);
                }
            } else if (msg.what == 2) {
                progressbar.setVisibility(GONE);
            }
        }
    };

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        progressbar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                10, 0, 0));

        Drawable drawable = context.getResources().getDrawable(R.drawable.progress_bar_states);
        progressbar.setProgressDrawable(drawable);
        addView(progressbar);
        // setWebViewClient(new WebViewClient(){});
        setWebChromeClient(new WebChromeClient());
        //是否可以缩放
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        handler.sendEmptyMessage(0);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                handler.sendEmptyMessageDelayed(1, 1);
            }
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void resetWebviewUI(){
        curProgress = 0;
        progressbar.setVisibility(View.VISIBLE);
        handler.sendEmptyMessage(0);
    }
}
