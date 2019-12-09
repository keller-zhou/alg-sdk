package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by keller on 16/6/6.
 */
public class MyCameraPhotosAdapter extends RecyclerView.Adapter {

    public static ImageView onClickImage;

    private ColorDrawable defaultPlaceholder = new ColorDrawable(0xff898989);
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnFail(defaultPlaceholder)
            .showImageOnLoading(defaultPlaceholder)
            .cacheInMemory(false)
            .cacheOnDisk(false)
            .build();

    private ArrayList<String> paths = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public MyCameraPhotosAdapter() {}



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_mycamera_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String url = paths.get(position);
        ((ViewHolder) holder).progressbar.setVisibility(View.VISIBLE);
        String showImagePath = "file://" + url;

        ImageLoader.getInstance().displayImage(showImagePath, ((ViewHolder) holder).imageView, options, new SimpleImageLoadingListener() {
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
                itemClickListener.onItemClick(url, position);
                onClickImage = ((ViewHolder) holder).imageView;
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }


    /**
     * 添加一张照片显示
     * @param path
     */
    public void addPath(String path) {
        this.paths.add(path);
        notifyItemInserted(paths.size() - 1);
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface ItemClickListener {
        public void onItemClick(String url, int position);
    }

}
