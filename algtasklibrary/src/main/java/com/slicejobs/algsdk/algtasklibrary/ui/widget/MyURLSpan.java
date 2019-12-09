package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;

import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.model.SerializableBaseMap;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.WebviewActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class MyURLSpan extends ClickableSpan implements ParcelableSpan {

    private final String mURL;

    public MyURLSpan(String url) {
        mURL = url;
    }

    public MyURLSpan(Parcel src) {
        mURL = src.readString();
    }

    /**
     * android6.0隐藏方法
     * @return
     */
    public int getSpanTypeIdInternal() {
        return 6; //@hide public static final int UNDERLINE_SPAN = 6;
    }

    public void writeToParcelInternal(Parcel dest, int flags) {

    }


    public int getSpanTypeId() {
        return 11;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
    }

    public String getURL() {
        return mURL;
    }

    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        Context context = widget.getContext();
        String url = null;
        if (uri.getScheme() == null) {
            url = "http://" + uri.toString();
        } else if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
            url = uri.toString();

            if (uri.toString().contains("$_userid")) {//允许替换用户ID
                String userid = BizLogic.getCurrentUser().userid;
                url = url.replace("$_userid", userid);
            }

            if (uri.toString().contains("$_cellphone")) {//允许替换用户手机号
                String cellphone = BizLogic.getCurrentUser().cellphone;
                url = url.replace("$_cellphone", cellphone);
            }

        }


        context.startActivity(WebviewActivity.getStartIntent(context, url));
    }
}
