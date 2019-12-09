package com.slicejobs.algsdk.algtasklibrary.view;

import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.TaskPresenter;

import java.util.List;

/**
 * Created by nlmartian on 7/27/15.
 */
public interface ITaskView extends IBaseView {
    public void showTaskList(List<Task> taskList, int start);

    public void getTaskError();

    public void resetAccountDialog();

    public void serverExecption(String source, TaskPresenter.OrderBy orderBy, int start, String status, boolean today, String latitude, String longitude, String disctance);

    public void getMyTaskList(List<Task> taskList, int start);

    public void showKeywordTaskList(List<Task> taskList);
}
