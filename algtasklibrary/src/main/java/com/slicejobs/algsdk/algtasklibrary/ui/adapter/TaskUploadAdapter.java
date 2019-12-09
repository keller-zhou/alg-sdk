package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TaskUploadAdapter extends BaseAdapter {

    private Context context;
    private List<String> stepIndexList;
    private String uploadStatus;

    public TaskUploadAdapter(Context context, String uploadStatus) {
        this.context = context;
        this.uploadStatus = uploadStatus;
        this.stepIndexList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return stepIndexList.size();
    }

    @Override
    public Object getItem(int position) {
        return stepIndexList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder=new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task_upload, null);
            holder.stepIndex = (TextView)convertView.findViewById(R.id.step_index);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.upload_progress);
            holder.uploadStatus = (TextView)convertView.findViewById(R.id.upload_status);
            convertView.setTag(holder);
        } else {
            holder=(ViewHolder)convertView.getTag();
        }
        holder.stepIndex.setText(stepIndexList.get(position));
        if(StringUtil.isNotBlank(uploadStatus) && uploadStatus.equals("wait")) {
            holder.progressBar.setProgress(0);
            holder.uploadStatus.setText("等待上传");
            holder.uploadStatus.setTextColor(Color.parseColor("#444444"));
        }else {
            holder.progressBar.setProgress(100);
            holder.uploadStatus.setText("上传成功");
            holder.uploadStatus.setTextColor(Color.parseColor("#0EC300"));
        }
        return convertView;
    }

    private class ViewHolder {
        public TextView stepIndex;
        public ProgressBar progressBar;
        public TextView uploadStatus;
    }

    public List<String> getStepIndexList() {
        return stepIndexList;
    }

    public void setStepIndexList(List<String> stepIndexList) {
        this.stepIndexList = stepIndexList;
        notifyDataSetChanged();
    }
}
