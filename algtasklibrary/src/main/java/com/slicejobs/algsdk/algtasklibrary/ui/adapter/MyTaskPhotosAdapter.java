package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.TaskImage;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by keller on 16/6/6.
 */
public class MyTaskPhotosAdapter extends RecyclerView.Adapter {

    private ColorDrawable defaultPlaceholder = new ColorDrawable(0xff898989);
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnFail(defaultPlaceholder)
            .showImageOnLoading(defaultPlaceholder)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    private ArrayList<TaskImage> taskImages = new ArrayList<TaskImage>();
    private ItemClickListener itemClickListener;

    public MyTaskPhotosAdapter() {}



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_mytask_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TaskImage taskImage = taskImages.get(position);
        if(taskImage != null) {
            String url = taskImage.getPath();
            ((ViewHolder) holder).progressbar.setVisibility(View.VISIBLE);
            if(!url.startsWith("http")) {
                url = "file://" + url;
            }else {
                url = url + "?x-oss-process=style/task_photo_thumbnail_w480";
            }

            ImageLoader.getInstance().displayImage(url, ((ViewHolder) holder).imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ((ViewHolder) holder).progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    ((ViewHolder) holder).progressbar.setVisibility(View.GONE);
                }
            });

            holder.itemView.setOnClickListener(v -> {//点击相机中的图片
                if (itemClickListener != null) {
                    TaskImage clickTaskImage = taskImages.get(position);
                    itemClickListener.onItemImageClick(clickTaskImage, position);
                }
            });

            ((ViewHolder) holder).imageCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (itemClickListener != null) {
                        TaskImage clickTaskImage = taskImages.get(position);
                        clickTaskImage.setIfSelect(isChecked);
                        itemClickListener.onItemSlectClick(clickTaskImage, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return taskImages.size();
    }

    /**
     * 添加一张照片显示
     * @param taskImage
     */
    public void addImage(TaskImage taskImage) {
        this.taskImages.add(taskImage);
    }

    /**
     * 更新选中状态
     */
    public void updataSelectState(ArrayList<String> selectUrls){
        if(selectUrls == null || selectUrls.size() == 0){
            return;
        }
        for (int i = 0; i < taskImages.size(); i++) {
            for (int j = 0; j < selectUrls.size(); j++) {
                if(taskImages.get(i).getPath().equals(selectUrls.get(j))){
                    taskImages.get(i).setIfSelect(true);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.image)
        ImageView imageView;
        @BindView(R2.id.progress_bar)
        View progressbar;
        @BindView(R2.id.image_item_framelayout)
        FrameLayout itemLayout;
        @BindView(R2.id.checkbox)
        CheckBox imageCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface ItemClickListener {
        void onItemImageClick(TaskImage taskImage, int position);
        void onItemSlectClick(TaskImage taskImage, int position);
    }

}
