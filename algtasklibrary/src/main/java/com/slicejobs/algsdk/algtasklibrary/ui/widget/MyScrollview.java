package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * 拦截ScrollView中的滚动事件
 */
public class MyScrollview extends ScrollView {
    private int downX;
    private int downY;
    private int mTouchSlop;
    private OnScrollToTopListener onScrollToTop;

    public MyScrollview(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) e.getRawX();
                downY = (int) e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) e.getRawY();
                if (Math.abs(moveY - downY) > mTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(e);
    }


    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
                                  boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollY == 0 && null != onScrollToTop) {
            onScrollToTop.onScrollTopListener(clampedY);
        } else if (scrollY != 0 && null != onScrollToTop) {
            onScrollToTop.onScrollBottomListener(clampedY);
        }
    }

    public void setOnScrollToTopLintener(OnScrollToTopListener listener) {
        onScrollToTop = listener;
    }

    public interface OnScrollToTopListener {
        public void onScrollTopListener(boolean isTop);
        public void onScrollBottomListener(boolean isBottom);

    }
}