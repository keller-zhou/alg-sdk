package com.slicejobs.algsdk.algtasklibrary.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.TempUploadTask;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.TaskUploadAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.MyListView;

import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

public class TaskUploadProgressFragment extends BaseFragment {
    @BindView(R2.id.img_cacel)
    ImageView cancel;
    @BindView(R2.id.layout_uploading)
    LinearLayout uploadingLayout;
    @BindView(R2.id.uploading_progress)
    ProgressBar uploadingProgressBar;
    @BindView(R2.id.currentStepIndex)
    TextView currentStepIndex;
    @BindView(R2.id.tv_uploading)
    TextView tvUploading;
    @BindView(R2.id.retry_btn)
    Button retryBtn;
    @BindView(R2.id.listview_wait)
    MyListView listViewWait;
    @BindView(R2.id.listview_uploaded)
    MyListView listViewUploaded;
    @BindView(R2.id.go_upload_cache)
    TextView goUploadCache;

    private TaskUploadAdapter waitUploadAdapter;
    private TaskUploadAdapter uploadedAdapter;

    private TaskUploadEventListener taskUploadEventListener;

    private String updateingIndex;
    private int currentProgress = 101;
    private String resultJson;
    private boolean isUploadFail;
    private TempUploadTask failTempUploadTask;
    private boolean isShowToCacheUpload;
    private boolean isUpdate;

    public boolean isShowToCacheUpload() {
        return isShowToCacheUpload;
    }

    public void setShowToCacheUpload(boolean showToCacheUpload) {
        if(isAdded()) {
            isShowToCacheUpload = showToCacheUpload;
            if (isShowToCacheUpload) {
                goUploadCache.setVisibility(View.VISIBLE);
            } else {
                goUploadCache.setVisibility(View.GONE);
            }
        }
    }

    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
        if(isAdded()){
            if(isUpdate){
                tvUploading.setText("正在更新");
            }else {
                tvUploading.setText("正在上传");
            }
        }
    }

    public String getUpdateingIndex() {
        return updateingIndex;
    }

    public void setUpdateingIndex(String updateingIndex) {
        this.updateingIndex = updateingIndex;
        if(isAdded()) {
            currentStepIndex.setText(this.updateingIndex);
        }
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        if(isAdded()) {
            uploadingProgressBar.setProgress(currentProgress);
        }
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public boolean isUploadFail() {
        return isUploadFail;
    }

    public void setWaitUploadList(ArrayList<String> waitUploadList) {
        if(isAdded()) {
            waitUploadAdapter.setStepIndexList(waitUploadList);
        }
    }

    public void setUploadedList(ArrayList<String> uploadedList) {
        if(isAdded()) {
            uploadedAdapter.setStepIndexList(uploadedList);
        }
    }

    public TempUploadTask getFailTempUploadTask() {
        return failTempUploadTask;
    }

    public void setFailTempUploadTask(TempUploadTask failTempUploadTask) {
        this.failTempUploadTask = failTempUploadTask;
    }

    public void setUploadFail(boolean uploadFail) {
        isUploadFail = uploadFail;
        if(isUploadFail){
            if(isAdded()) {
                tvUploading.setVisibility(View.GONE);
                retryBtn.setVisibility(View.VISIBLE);
                setProgressDrawable(uploadingProgressBar, R.drawable.progress_horizontal_error_bg);
                uploadingProgressBar.setProgress(100);
            }
        }else {
            if (isAdded()) {
                tvUploading.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                setProgressDrawable(uploadingProgressBar, R.drawable.progress_horizontal_bg);
                uploadingProgressBar.setProgress(currentProgress);
            }
        }
    }

    public void setTaskUploadEventListener(TaskUploadEventListener taskUploadEventListener) {
        this.taskUploadEventListener = taskUploadEventListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_multi_upload_status, container, false);
        ButterKnife.bind(this, view);
        waitUploadAdapter = new TaskUploadAdapter(getActivity(),"wait");
        uploadedAdapter = new TaskUploadAdapter(getActivity(),"uploaded");
        listViewWait.setAdapter(waitUploadAdapter);
        listViewUploaded.setAdapter(uploadedAdapter);
        Bundle fragmentArgs = getArguments();
        if(fragmentArgs != null){
            if(fragmentArgs.getStringArrayList("waitStepIndexs") != null){
                waitUploadAdapter.setStepIndexList(fragmentArgs.getStringArrayList("waitStepIndexs"));
            }
            if(fragmentArgs.getStringArrayList("uploadedStepIndexs") != null){
                uploadedAdapter.setStepIndexList(fragmentArgs.getStringArrayList("uploadedStepIndexs"));
            }
            isShowToCacheUpload = fragmentArgs.getBoolean("isShowToCacheUpload");
        }
        if(isUploadFail){
            uploadingLayout.setVisibility(View.VISIBLE);
            tvUploading.setVisibility(View.GONE);
            retryBtn.setVisibility(View.VISIBLE);
            setProgressDrawable(uploadingProgressBar, R.drawable.progress_horizontal_error_bg);
            uploadingProgressBar.setProgress(100);
        }else {
            if(currentProgress < 100) {
                uploadingLayout.setVisibility(View.VISIBLE);
                tvUploading.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                setProgressDrawable(uploadingProgressBar, R.drawable.progress_horizontal_bg);
                uploadingProgressBar.setProgress(currentProgress);
                currentStepIndex.setText(this.updateingIndex);
                if (isUpdate) {
                    tvUploading.setText("正在更新");
                } else {
                    tvUploading.setText("正在上传");
                }
            }
        }
        if(isShowToCacheUpload){
            goUploadCache.setVisibility(View.VISIBLE);
        }else {
            goUploadCache.setVisibility(View.GONE);
        }
        view.setClickable(true);
        return view;
    }

    @OnClick({R2.id.img_cacel, R2.id.retry_btn, R2.id.go_upload_cache})
    public void onClick(View view) {
        if (view.getId() == R.id.img_cacel) {
            if(taskUploadEventListener != null){
                taskUploadEventListener.onClose();
            }
        } else if (view.getId() == R.id.retry_btn) {
            if(taskUploadEventListener != null){
                taskUploadEventListener.onRetryClick(failTempUploadTask);
            }
        } else if(view.getId() == R.id.go_upload_cache){
            if(taskUploadEventListener != null){
                taskUploadEventListener.onGoUploadCacheClick();
            }
        }
    }

    public void refreshUploadingStatus(boolean isFinish){
        if(isAdded()) {
            if (isFinish) {
                uploadingLayout.setVisibility(View.GONE);
            } else {
                uploadingLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface TaskUploadEventListener{
        public void onClose();
        public void onRetryClick(TempUploadTask failTempUploadTask);
        public void onGoUploadCacheClick();
    }

    @SuppressLint("NewApi")
    public static void setProgressDrawable(ProgressBar bar, int resId) {
        Drawable layerDrawable = bar.getResources().getDrawable(resId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable d = getMethod("tileify", bar, new Object[] { layerDrawable, false });
            bar.setProgressDrawable(d);
        } else {
            bar.setProgressDrawableTiled(layerDrawable);
        }
    }

    private static Drawable getMethod(String methodName, Object o, Object[] paras) {
        Drawable newDrawable = null;
        try {
            Class<?> c[] = new Class[2];
            c[0] = Drawable.class;
            c[1] = boolean.class;
            Method method = ProgressBar.class.getDeclaredMethod(methodName, c);
            method.setAccessible(true);
            newDrawable = (Drawable) method.invoke(o, paras);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return newDrawable;
    }
}
