package com.slicejobs.algsdk.algtasklibrary.view;

/**
 * Created by nlmartian on 7/9/15.
 */
public interface IBaseView {
    public void showProgressDialog();

    public void dismissProgressDialog();

    public void toast(String msg);
}
