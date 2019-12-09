package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nlmartian on 7/19/15.
 */
public class TaskListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Task> taskList = new ArrayList<>();

    private ItemClickCallback callback;

    public static final String SOURCE_NEW_TASK = "1";//最新任务

    public static final String SOURCE_MY_TASK = "2";//我的任务

    public static final String SOURCE_MAP_TASK = "3";//地图任务

    public static final String SOURCE_NEAR_TASK = "4";//周边任务

    public static final int TYPE_MAP = 3;//地图任务

    public static final int TYPE_NEAR_TASK = 4;//周边任务

    public static final int ITEM_TASK = 0;

    public static final int ITEM_VIEW_ARCHIVED = 1;

    private int  sourceType ;

    private String orderRule = "default";//排序规则 门店:1按赏金 2按零豆  3按时间   我的任务:4未完成 5已提交 6已审核

    public static final String BY_NOT_COMPLETE = "4";

    public static final String BY_SUBMIT = "5";

    public static final String BY_COMPLETE = "6";



    public TaskListAdapter() {
    }

    public TaskListAdapter(int sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = null;
        if (viewType == ITEM_TASK) {
            view = LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false);
            return new ViewHolder(view);
        } else if (viewType == ITEM_VIEW_ARCHIVED) {
            view = LayoutInflater.from(context).inflate(R.layout.item_view_archived, parent, false);
            return new FooterViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            TaskListAdapter.ViewHolder taskViewHolder = (TaskListAdapter.ViewHolder) holder;
            Task task = taskList.get(position);
            taskViewHolder.title.setText(task.getTitle());
            String strTime = DateUtil.getListDuration(task.getStarttime(), task.getEndtime());

            taskViewHolder.time.setText(strTime);
            if (task.getMarketinfo() != null) {
                taskViewHolder.market.setText(task.getMarketinfo().getMarketname());
            } else {
                if (task.getLatitude() == null || task.getLongitude() == null) {
                    taskViewHolder.market.setText(PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.CURRENT_CITY)+"城市任务");
                }
                if (StringUtil.isNotBlank(task.getProvinceid()) && task.getProvinceid().equals("-1")) {
                    taskViewHolder.market.setText(SliceApp.CONTEXT.getString(R.string.text_nationwide_task));
                }
            }
            taskViewHolder.salary.setText(SliceApp.CONTEXT.getString(R.string.fmt_balance, task.getSalary()));

            taskViewHolder.itemView.setOnClickListener(view -> {
                if (callback != null) {
                    callback.onItemClick(view, task, position);
                }
            });

            taskViewHolder.iconBg.setBackgroundResource(getTaskIconBg(Integer.valueOf(StringUtil.isBlank(task.getType()) ? "4" : task.getType())));
            taskViewHolder.iconTxt.setText(getTaskTypeRes(Integer.valueOf(StringUtil.isBlank(task.getType()) ? "4" : task.getType())));

            if ((sourceType == TYPE_MAP || sourceType == TYPE_NEAR_TASK) && task.getStatus().equals("1") && task.getDistance() != null) {
                taskViewHolder.distance.setVisibility(View.VISIBLE);
                DecimalFormat df = new DecimalFormat("0.00");
                taskViewHolder.distance.setText(SliceApp.CONTEXT.getString(R.string.hint_task_distance, df.format(Double.parseDouble(task.getDistance()))));
            }
            if (null != task.getEndtime()) {
                int diffTime = DateUtil.getTaskRemainTime(task.getEndtime().getTime());
                if (diffTime >= 0 && diffTime < 8 && StringUtil.isNotBlank(task.getStatus()) && (task.getStatus().equals("2") || task.getStatus().equals("3") || task.getStatus().equals("-1"))) {
                    taskViewHolder.hintOver.setImageResource(R.drawable.ic_task_fastend);
                    taskViewHolder.hintOver.setVisibility(View.VISIBLE);
                } else if (diffTime < 0 && StringUtil.isNotBlank(task.getStatus()) && (task.getStatus().equals("2") || task.getStatus().equals("3") || task.getStatus().equals("-1"))) {
                    taskViewHolder.hintOver.setImageResource(R.drawable.ic_task_yetend);
                    taskViewHolder.hintOver.setVisibility(View.VISIBLE);
                } else {
                    taskViewHolder.hintOver.setVisibility(View.GONE);
                }
            }

            if (StringUtil.isNotBlank(task.getErrormessage()) && task.getErrormessage().length() > 0 && (task.getStatus().equals("3") || task.getStatus().equals("-1"))) {//审核未通过
                taskViewHolder.errorTaskImg.setVisibility(View.VISIBLE);
            } else {
                taskViewHolder.errorTaskImg.setVisibility(View.GONE);
            }
            if (Integer.parseInt(task.getPriority()) > 0 && !orderRule.equals(BY_SUBMIT) && !orderRule.equals(BY_COMPLETE)) {//置顶
                taskViewHolder.taskStick.setVisibility(View.VISIBLE);
            } else {
                taskViewHolder.taskStick.setVisibility(View.GONE);
            }
        } else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.itemView.setOnClickListener(view -> {

            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Task task = taskList.get(position);
        if ("archived".equals(task.getTaskid())) {
            return ITEM_VIEW_ARCHIVED;
        } else {
            return ITEM_TASK;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private String getTaskStatusText(Context context, String status) {
        String strStatus = null;
        if ("2".equals(status)) {
            strStatus = context.getString(R.string.task_status_assign);
        } else if ("3".equals(status)) {
            strStatus = context.getString(R.string.task_status_process);
        } else if ("4".equals(status)) {
            strStatus = context.getString(R.string.task_status_finished);
        } else if ("-2".equals(status)) {
            strStatus = context.getString(R.string.task_status_failed);
        } else if ("-1".equals(status)) {
            strStatus = context.getString(R.string.task_closed);
        }
        return strStatus;
    }

    /**
     * 工单筛选
     * @param tasks
     * @param sourceType
     */
    public void updateTasks(List<Task> tasks, String sourceType) {
        if (sourceType.equals(SOURCE_NEW_TASK) && tasks != null)  {
            for (int index = 0; index < tasks.size(); index ++) {
                if (!tasks.get(index).getStatus().equals("1")) {
                    tasks.remove(index);
                }
            }
        }

        //已提交,已完成,不需要优先级
        if((sourceType.equals(SOURCE_MY_TASK) || sourceType.equals(SOURCE_NEW_TASK) || sourceType.equals(SOURCE_MAP_TASK) || sourceType.equals(SOURCE_NEAR_TASK)) && (!orderRule.equals(BY_SUBMIT) && !orderRule.equals(BY_COMPLETE))) {
            Collections.sort(tasks, (task, t1) -> {
                //按照工单优先级
                if (Integer.parseInt(task.getPriority()) <  Integer.parseInt(t1.getPriority())) {
                    return 1;
                }
                if (Integer.parseInt(task.getPriority()) ==  Integer.parseInt(t1.getPriority())) {
                    return 0;
                }
                return -1;
            });
        }
        updateTasks(tasks);
    }

    public void updateTasks(List<Task> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        notifyDataSetChanged();
    }

    public void clearTasks() {
        taskList.clear();
        notifyDataSetChanged();
    }

    public void addTasks(List<Task> tasks, String sourceType) {
        if (sourceType.equals(SOURCE_NEW_TASK)) {
            for (int index = 0; index < tasks.size(); index ++) {
                if (!tasks.get(index).getStatus().equals("1")) {
                    tasks.remove(index);
                }
            }
        }
        addTasks(tasks);
    }

    public void addTasks(List<Task> tasks) {
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < tasks.size(); j++) {//不能有重复添加
                if (taskList.size() > 0 && tasks.get(j).getTaskid().equals(taskList.get(i).getTaskid())) {
                    tasks.remove(j);
                }
            }
        }
        taskList.addAll(tasks);

        if (!orderRule.equals(BY_SUBMIT) && !orderRule.equals(BY_COMPLETE)) {
            Collections.sort(taskList, (task, t1) -> {//优先级排序
                //按照工单优先级
                if (Integer.parseInt(task.getPriority()) <  Integer.parseInt(t1.getPriority())) {
                    return 1;
                }
                if (Integer.parseInt(task.getPriority()) ==  Integer.parseInt(t1.getPriority())) {
                    return 0;
                }
                return -1;
            });
        }

        notifyDataSetChanged();
    }

    public void setCallback(ItemClickCallback callback) {
        this.callback = callback;
    }

    public void updateTaskStatus(String taskid, String status) {
        for (int i = 0; i < taskList.size(); i++){
            Task task = taskList.get(i);
            if (StringUtil.isNotBlank(task.getTaskid()) && task.getTaskid().equals(taskid)) {
                task.setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateTask(String taskId, Task newTask) {
        for (int i = 0; i < taskList.size(); i++){
            Task task = taskList.get(i);
            if (StringUtil.isNotBlank(task.getTaskid()) && task.getTaskid().equals(taskId)) {
                taskList.set(i, newTask);
                notifyItemChanged(i);
                break;
            }
        }
    }


    /**
     * 设置排序规则,以免被优先级打乱
     */
    public void setOrderRule(String rule) {
        this.orderRule = rule;
    }


    /**
     * 理货
     招聘
     促销
     派发
     市调
     其他
     * @param type
     * @return
     */
    private int getTaskIconBg(int type) {
        switch (type) {
            case 0:
                return R.drawable.shape_task_org;
            case 1:
                return R.drawable.shape_task_hire;
            case 2:
                return R.drawable.shape_task_promotion;
            case 3:
                return R.drawable.shape_task_distribute;
            case 4:
                return R.drawable.shape_task_survey;
            case 5:
                return R.drawable.shape_task_data;
            case 6:
                return R.drawable.shape_task_offer;
            case 7:
                return R.drawable.shape_task_audit;
            case 8:
                return R.drawable.shape_task_entering;
            case 9:
                return R.drawable.shape_task_other;
            case 10:
                return R.drawable.shape_task_other;
            case 11:
                return R.drawable.shape_task_experience;
            case 12:
                return R.drawable.shape_task_interaction;
            case 13:
                return R.drawable.shape_task_share;
            case 14:
                return R.drawable.shape_task_survey;
            case 15:
                return R.drawable.shape_task_promotion;
            case 16:
                return R.drawable.shape_task_data;
            case 17:
                return R.drawable.shape_task_research;
            case 18:
                return R.drawable.shape_task_extend;
        }
        return R.drawable.shape_task_org;
    }

    private int getTaskTypeRes(int type) {
        switch (type) {
            case 0:
                return R.string.task_type_org;
            case 1:
                return R.string.task_type_hire;
            case 2:
                return R.string.task_type_promotion;
            case 3:
                return R.string.task_type_distribute;
            case 4:
                return R.string.task_type_survey;
            case 5:
                return R.string.task_type_data;
            case 6:
                return R.string.task_type_offer;
            case 7:
                return R.string.task_type_audit;
            case 8:
                return R.string.task_type_entering;
            case 9:
                return R.string.task_type_other;
            case 10:
                return R.string.task_type_other;
            case 11:
                return R.string.task_type_experience;
            case 12:
                return R.string.task_type_interaction;
            case 13:
                return R.string.task_type_share;
            case 14:
                return R.string.task_type_survey;
            case 15:
                return R.string.task_type_activity;
            case 16:
                return R.string.task_type_exam;
            case 17:
                return R.string.task_type_research;
            case 18:
                return R.string.task_type_expand;
        }
        return R.string.task_type_org;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.salary)
        TextView salary;
        @BindView(R2.id.time)
        TextView time;
        @BindView(R2.id.market)
        TextView market;
        @BindView(R2.id.icon_bg)
        View iconBg;
        @BindView(R2.id.icon_text)
        TextView iconTxt;
        @BindView(R2.id.distance)
        TextView distance;
        @BindView(R2.id.hint_over)
        ImageView hintOver;
        @BindView(R2.id.error_task_image)
        ImageView errorTaskImg;
        @BindView(R2.id.tv_task_stick)
        TextView taskStick;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface ItemClickCallback {
        public void onItemClick(View view, Task task, int position);
    }

}
