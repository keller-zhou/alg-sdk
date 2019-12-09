package com.slicejobs.algsdk.algtasklibrary.view;

/**
 * Created by nlmartian on 7/12/15.
 */
public interface IRegisterView extends IBaseView {

    public void sendVCodeSuccess();

    public void sendVCodeFaild(String msg);

    public void registerFail();

}
