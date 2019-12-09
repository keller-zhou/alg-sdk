package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import com.slicejobs.algsdk.algtasklibrary.net.RestClient;

/**
 * Created by nlmartian on 7/9/15.
 */
public class BasePresenter {
    protected RestClient restClient = RestClient.getInstance();

    //protected Api api = RestClient.getInstance().provideApi();
}
