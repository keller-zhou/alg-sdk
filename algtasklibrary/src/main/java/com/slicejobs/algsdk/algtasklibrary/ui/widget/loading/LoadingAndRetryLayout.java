package com.slicejobs.algsdk.algtasklibrary.ui.widget.loading;

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;

public class LoadingAndRetryLayout extends FrameLayout
{
    private View mLoadingView;
    private View mRetryView;
    private View mContentView;
    private View mEmptyView;
    private LayoutInflater mInflater;
    private Context context;
    private AlphaAnimation mHideAnimation;
    private static final String TAG = LoadingAndRetryLayout.class.getSimpleName();


    public LoadingAndRetryLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        context = context;
        mInflater = LayoutInflater.from(context);
    }


    public LoadingAndRetryLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
        this.context = context;
    }

    public LoadingAndRetryLayout(Context context)
    {
        this(context, null);
        this.context = context;
    }

    private boolean isMainThread()
    {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public void showLoading()
    {
        if (isMainThread())
        {
            showView(mLoadingView);
        } else
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    showView(mLoadingView);
                }
            });
        }
    }

    public void showRetry()
    {
        if (isMainThread())
        {
            showView(mRetryView);
        } else
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    showView(mRetryView);
                }
            });
        }

    }

    public void showContent()
    {
        if (isMainThread())
        {
            showView(mContentView);
        } else
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    showView(mContentView);
                }
            });
        }
    }

    public void showEmpty()
    {
        if (isMainThread())
        {
            showView(mEmptyView);
        } else
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    showView(mEmptyView);
                }
            });
        }
    }


    private void showView(View view)
    {
        if (view == null) return;

        if (view == mLoadingView)
        {
            mLoadingView.setVisibility(View.VISIBLE);
            if (mRetryView != null)
                mRetryView.setVisibility(View.GONE);
            /*if (mContentView != null)
                mContentView.setVisibility(View.GONE);*/
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);
        } else if (view == mRetryView)
        {
            mRetryView.setVisibility(View.VISIBLE);
            if (mLoadingView != null)
                mLoadingView.setVisibility(View.GONE);
            /*if (mContentView != null)
                mContentView.setVisibility(View.GONE);*/
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);
        } else if (view == mContentView)
        {
            mContentView.setVisibility(View.VISIBLE);
            if (mLoadingView != null)
                //mLoadingView.setVisibility(View.GONE);
                setHideAnimation(mLoadingView,500);
            if (mRetryView != null)
                mRetryView.setVisibility(View.GONE);
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);
        } else if (view == mEmptyView)
        {
            mEmptyView.setVisibility(View.VISIBLE);
            if (mLoadingView != null)
                mLoadingView.setVisibility(View.GONE);
            if (mRetryView != null)
                mRetryView.setVisibility(View.GONE);
            /*if (mContentView != null)
                mContentView.setVisibility(View.GONE);*/
        }


    }

    public View setContentView(int layoutId)
    {
        return setContentView(mInflater.inflate(layoutId, this, false));
    }

    public View setLoadingView(boolean isActivity, int layoutId)
    {
        if(layoutId == LoadingAndRetryManager.BASE_LOADING_LAYOUT_ID){
            View view =  mInflater.inflate(layoutId, this, false);
            if(isActivity) {
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                params.topMargin = DensityUtil.getStatusBarHeight(context) + DensityUtil.dip2px(context, 43);
                view.setLayoutParams(params);
            }

            return setLoadingView(view);
        }
        return setLoadingView(mInflater.inflate(layoutId, this, false));
    }

    public View setEmptyView(int layoutId)
    {
        return setEmptyView(mInflater.inflate(layoutId, this, false));
    }

    public View setRetryView(boolean isActivity, int layoutId)
    {
        View view =  mInflater.inflate(layoutId, this, false);
        if(isActivity) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.topMargin = DensityUtil.getStatusBarHeight(context) + DensityUtil.dip2px(context, 43);
            view.setLayoutParams(params);
        }
        return setRetryView(view);
    }
    public View setLoadingView(View view)
    {
        View loadingView = mLoadingView;
        if (loadingView != null)
        {
            Log.w(TAG, "you have already set a loading view and would be instead of this new one.");
        }
        removeView(loadingView);
        addView(view);
        mLoadingView = view;
        return mLoadingView;
    }

    public View setEmptyView(View view)
    {
        View emptyView = mEmptyView;
        if (emptyView != null)
        {
            Log.w(TAG, "you have already set a empty view and would be instead of this new one.");
        }
        removeView(emptyView);
        addView(view);
        mEmptyView = view;
        return mEmptyView;
    }

    public View setRetryView(View view)
    {
        View retryView = mRetryView;
        if (retryView != null)
        {
            Log.w(TAG, "you have already set a retry view and would be instead of this new one.");
        }
        removeView(retryView);
        addView(view);
        mRetryView = view;
        return mRetryView;

    }

    public View setContentView(View view)
    {
        View contentView = mContentView;
        if (contentView != null)
        {
            Log.w(TAG, "you have already set a retry view and would be instead of this new one.");
        }
        removeView(contentView);
        addView(view);
        mContentView = view;
        return mContentView;
    }

    public View getRetryView()
    {
        return mRetryView;
    }

    public View getLoadingView()
    {
        return mLoadingView;
    }

    public View getContentView()
    {
        return mContentView;
    }

    public View getEmptyView()
    {
        return mEmptyView;
    }

    /**
     * View渐隐动画效果
     */
    public  void setHideAnimation(View view, int duration)
    {
        if (null == view || duration < 0)
        {
            return;
        }

        if (null != mHideAnimation)
        {
            mHideAnimation.cancel();
        }
        // 监听动画结束的操作
        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideAnimation.setDuration(duration);
        mHideAnimation.setFillAfter(true);
        view.startAnimation(mHideAnimation);
    }
}
