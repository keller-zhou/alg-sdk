package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.Upload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keller.zhou on 8/22/15.
 * 上传缓存
 */
public class UploadCacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    private  ItemClickListener itemClickListener;

    private List<Upload> tasks = new ArrayList<>();

    private RecyclerView recyclerView;

    private ConcurrentHashMap<String, Integer> fileUploadProgressMap = new ConcurrentHashMap<>();//用户记录上传进度,



    public UploadCacheAdapter(ItemClickListener itemClickListener, RecyclerView recyclerView) {
        this.itemClickListener = itemClickListener;
        this.recyclerView = recyclerView;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upload_evidence, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = ((ViewHolder) holder);
        Upload upload = tasks.get(position);
        itemHolder.path = upload.getPath();//绑定视图与文件当前进度
        Integer pro = fileUploadProgressMap.get(upload.getPath());

        if(upload.getType().equals("photo")){
            itemHolder.ivPic.setImageResource(R.drawable.ic_photo_image);
            itemHolder.tvID.setText("图片");
        }else if(upload.getType().equals("record")){
            itemHolder.ivPic.setImageResource(R.drawable.ic_record);
            itemHolder.tvID.setText("录音");
        }else if(upload.getType().equals("video")){
            itemHolder.ivPic.setImageResource(R.drawable.icon_video);
            itemHolder.tvID.setText("视频");
        }else {

        }

        if (pro < 0) {
            itemHolder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_failed)));
            itemHolder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_error_red));
            itemHolder.btStartUpload.setVisibility(View.VISIBLE);
            itemHolder.btStartUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//点击之后，启动上传
                    itemHolder.btStartUpload.setVisibility(View.GONE);
                    itemHolder.btStartUpload.setOnClickListener(null);
                    fileUploadProgressMap.put(upload.getPath(), 0);
                    itemHolder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_status_wait)));
                    itemClickListener.onItemUploadButtonClick(position);
                }
            });
        } else if (pro == 0) {
            itemHolder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_status_wait)));
            itemHolder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.text_color1));
            if (itemHolder.btStartUpload.getVisibility() == View.VISIBLE) {itemHolder.btStartUpload.setVisibility(View.GONE);}
        } else if (pro < 100) {
            itemHolder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_progress, upload.getProgress() + "%"));
            itemHolder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.text_color1));
            if (itemHolder.btStartUpload.getVisibility() == View.VISIBLE) {itemHolder.btStartUpload.setVisibility(View.GONE);}
        } else {
            itemHolder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.uploadsucceed)));
            itemHolder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_correct_green));
            if (itemHolder.btStartUpload.getVisibility() == View.VISIBLE) {itemHolder.btStartUpload.setVisibility(View.GONE);}
        }
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }


    public void setUploadTask(List<Upload> list) {

        tasks.clear();
        fileUploadProgressMap.clear();
        tasks.addAll(list);
        for (Upload upload : list) {
            fileUploadProgressMap.put(upload.getPath(), upload.getProgress());
        }
        notifyDataSetChanged();
    }



    public void updateTaskStatus (String path, int pro, int index) {
        fileUploadProgressMap.put(path, pro);

        ViewHolder holder = findViewHolderByPath(path);
        if (holder != null) {
            if (pro < 0) {
                holder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_failed)));
                holder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_error_red));
                holder.btStartUpload.setVisibility(View.VISIBLE);
                holder.btStartUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//点击之后，启动上传
                        holder.btStartUpload.setVisibility(View.GONE);
                        holder.btStartUpload.setOnClickListener(null);
                        fileUploadProgressMap.put(path, 0);
                        holder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_status_wait)));
                        itemClickListener.onItemUploadButtonClick(index);
                    }
                });
            } else if (pro == 0) {
                holder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.upload_status_wait)));
                holder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.text_color1));
                if (holder.btStartUpload.getVisibility() == View.VISIBLE) {holder.btStartUpload.setVisibility(View.GONE);}
            } else if (pro < 100) {
                holder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_progress, pro + "%"));
                holder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.text_color1));
                if (holder.btStartUpload.getVisibility() == View.VISIBLE) {holder.btStartUpload.setVisibility(View.GONE);}
            } else if (pro > 100) {
                holder.tvStatus.setText(SliceApp.CONTEXT.getString(R.string.text_upload_status, SliceApp.CONTEXT.getString(R.string.uploadsucceed)));
                holder.tvStatus.setTextColor(SliceApp.CONTEXT.getResources().getColor(R.color.color_correct_green));
                if (holder.btStartUpload.getVisibility() == View.VISIBLE) {holder.btStartUpload.setVisibility(View.GONE);}
            }
        }
    }





    private ViewHolder findViewHolderByPath(String filePath) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof ViewHolder) {
                ViewHolder holder = (ViewHolder) tag;
                if (filePath.equals(holder.path)) {
                    return holder;
                }
            }
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.ivPic)
        ImageView ivPic;
        @BindView(R2.id.tvId)
        TextView tvID;
        @BindView(R2.id.tvStatus)
        TextView tvStatus;
        @BindView(R2.id.btn_start_upload)
        Button btStartUpload;

        String path;



        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }


    public interface ItemClickListener {
        void onItemUploadButtonClick(int index);
    }


}
