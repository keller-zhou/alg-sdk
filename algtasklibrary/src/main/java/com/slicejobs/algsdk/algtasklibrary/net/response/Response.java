package com.slicejobs.algsdk.algtasklibrary.net.response;

/**
 * Created by nlmartian on 7/12/15.
 */
public class Response<T> {
    public int ret;
    public String msg;
    public String version;
    public T detail;
}
