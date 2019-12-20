package com.slicejobs.algsdk.algtasklibrary.ui.adapter;


import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nlmartian on 8/22/15.
 */
public class RequirementPhotosAdapter extends RecyclerView.Adapter {
    public static ImageView onClickImage;

    public static final String FROM_LOCAL_TASKDETAIL = "taskdetail";

    public static final String FROM_LACAL_EXAMINE = "taskStepsExamine";

    private String currFromLocal;

    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnFail(R.drawable.ic_photo_default_show)
            .showImageOnLoading(R.drawable.ic_photo_default_show)
            .build();

    private ArrayList<String> urls = new ArrayList<>();
    private ItemClickListener itemClickListener;

    private RecyclerView recyclerView;

    public RequirementPhotosAdapter() {}

    public RequirementPhotosAdapter(String fromLocal, RecyclerView recyclerView) {
        currFromLocal = fromLocal;
        this.recyclerView = recyclerView;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_requirement_photo3, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String url = urls.get(position);
        ((ViewHolder) holder).progressbar.setVisibility(View.VISIBLE);
        ImageLoader.getInstance().displayImage(url, ((ViewHolder) holder).imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ((ViewHolder) holder).progressbar.setVisibility(View.GONE);
//                if (currFromLocal != null && currFromLocal.equals(FROM_LACAL_EXAMINE) && photoViewAttacher != null) {
//                    photoViewAttacher.update();
//                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                ((ViewHolder) holder).progressbar.setVisibility(View.GONE);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(url, position);
                    onClickImage = ((ViewHolder) holder).imageView;
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemLongClick(((ViewHolder) holder).imageView);
                }
                return false;
            }
        });

//        if (currFromLocal != null && currFromLocal.equals(FROM_LACAL_EXAMINE)) {
//            photoViewAttacher = new PhotoViewAttacher(((ViewHolder) holder).imageView);
//        }

        if (currFromLocal != null && (currFromLocal.equals(FROM_LOCAL_TASKDETAIL))) {
            return;
        }

        if (currFromLocal != null && currFromLocal.equals(FROM_LACAL_EXAMINE)) {//处理分布页面的
            if (urls.size() == 1) {
                int width = DensityUtil.screenWidthInPix(holder.itemView.getContext()) - DensityUtil.dip2px(holder.itemView.getContext(), 30);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        ((ViewHolder) holder).itemLayout.setLayoutParams(layoutParams);

                ((ViewHolder) holder).itemLayout.requestLayout();
                FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(
                        width, FrameLayout.LayoutParams.MATCH_PARENT);
                ((ViewHolder) holder).imageView.setLayoutParams(imageLayoutParams);
                ((ViewHolder) holder).imageBg.setVisibility(View.GONE);

            } else {
                int width =  DensityUtil.dip2px(holder.itemView.getContext(), 90);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(DensityUtil.dip2px(holder.itemView.getContext(), 5), 0, DensityUtil.dip2px(holder.itemView.getContext(), 5), 0);
                ((ViewHolder) holder).itemLayout.setLayoutParams(layoutParams);

                ((ViewHolder) holder).itemLayout.requestLayout();
                FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(
                        width, FrameLayout.LayoutParams.MATCH_PARENT);
                ((ViewHolder) holder).imageView.setLayoutParams(imageLayoutParams);
                ((ViewHolder) holder).imageBg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public void setUrls(List<String> urls) {
        if (urls == null) {
            return;
        }
        this.urls.clear();
        this.urls.addAll(urls);
        notifyDataSetChanged();
    }

    public void addUrl(String url) {
        this.urls.add(url);
        notifyItemInserted(urls.size() - 1);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

     class ViewHolder extends RecyclerView.ViewHolder {

         @BindView(R2.id.imageBg)
         TextView imageBg;
        @BindView(R2.id.image)
        ImageView imageView;
        @BindView(R2.id.progress_bar)
        View progressbar;
        @BindView(R2.id.image_item_framelayout)
        FrameLayout itemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
     }

    public interface ItemClickListener {
        public void onItemClick(String url, int position);

        public void onItemLongClick(ImageView imageView);//长按识别二维码
    }

}
