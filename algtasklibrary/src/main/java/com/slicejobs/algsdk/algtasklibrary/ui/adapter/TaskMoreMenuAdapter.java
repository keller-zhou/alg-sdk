package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.TaskMoreMenuItem;

import java.util.List;

public class TaskMoreMenuAdapter extends BaseAdapter {

    private Context context;
    private List<TaskMoreMenuItem> menuItemList;

    public TaskMoreMenuAdapter(Context context, List<TaskMoreMenuItem> menuItemList) {
        this.context = context;
        this.menuItemList = menuItemList;
    }

    @Override
    public int getCount() {
        return menuItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItemList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task_more_menu, null);
            holder.container = (LinearLayout) convertView.findViewById(R.id.container);
            holder.content = (LinearLayout) convertView.findViewById(R.id.content_layout);
            holder.divider = (TextView) convertView.findViewById(R.id.divider);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder=(ViewHolder)convertView.getTag();
        }
        TaskMoreMenuItem taskMoreMenuItem = menuItemList.get(position);
        if (menuItemList.size() == 1) {
            holder.container.setBackgroundResource(R.drawable.menu_bg_one_item);
            holder.container.setPadding(0, 35, 0, 0);
            holder.divider.setVisibility(View.GONE);
        } else {
            if (position == 0) {
                holder.container.setBackgroundResource(R.drawable.bg_more_menu_top);
                holder.divider.setVisibility(View.GONE);
                holder.container.setPadding(0, 30, 0, 0);
            } else if (position == menuItemList.size() - 1) {
                holder.container.setBackgroundResource(R.drawable.bg_more_menu_bottom);
                holder.divider.setVisibility(View.VISIBLE);
            } else {
                holder.container.setBackgroundResource(R.drawable.bg_more_menu_mid);
                holder.divider.setVisibility(View.VISIBLE);
            }
        }
        holder.text.setText(taskMoreMenuItem.getText());
        holder.icon.setBackgroundResource(taskMoreMenuItem.getIconId());

        return convertView;
    }

    private class ViewHolder {
        public LinearLayout container;
        public LinearLayout content;
        public TextView divider;
        public ImageView icon;
        public TextView text;
    }

}
