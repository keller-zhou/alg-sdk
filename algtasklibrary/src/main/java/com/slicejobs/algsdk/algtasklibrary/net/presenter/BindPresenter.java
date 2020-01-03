package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import android.os.Build;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.LoginRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.RegisterRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.net.response.ZddResponse;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IBindView;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 7/12/15.
 */
public class BindPresenter extends BasePresenter {

    private IBindView view;

    public BindPresenter(IBindView view) {
        this.view = view;
    }

    public void getVCode(String appId,String userId,String mobile,String actionTime,String sign) {
        view.showProgressDialog();
        Observable<Response<List>> registerOb = restClient.provideOpenApi().getVCode(appId, userId, mobile, actionTime, sign);

        registerOb.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Response<List>>() {
                    @Override
                    public void call(Response<List> res) {
                        view.dismissProgressDialog();
                        if (res.ret == 0) {
                            view.sendVCodeSuccess();
                        } else {
                            view.sendVCodeFaild(res.msg);
                        }
                    }
                }, e -> {
                    view.dismissProgressDialog();
                    try {
                        RetrofitError retrofitError = (RetrofitError) e;
                        if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.NETWORK.toString())) {
                            view.sendVCodeFaild("网络开小差了");
                        } else {
                            view.sendVCodeFaild(SliceApp.CONTEXT.getString(R.string.server_error));
                        }
                    } catch (IllegalStateException e1) {
                        view.sendVCodeFaild("网络开小差了哦");
                    }
                });
    }

    public void bind(String appId,String userId,String mobile,String actionTime,String sign, String vcode) {
        String timestamp = DateUtil.getCurrentTime();
        view.showProgressDialog();
        Observable<ZddResponse<LoginRes>> registerOb = restClient.provideOpenApi().bind(appId,userId,mobile,actionTime,sign, vcode);;

        registerOb.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ZddResponse<LoginRes>>() {
                    @Override
                    public void call(ZddResponse<LoginRes> res) {
                        view.dismissProgressDialog();
                        if (res.code == 0) {
                            PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, res.data);
                            String accessToken = res.data.authkey;
                            if (StringUtil.isBlank(accessToken) || accessToken.length() < 3) {//防止sj没有传递下来
                                view.toast(SliceApp.CONTEXT.getString(R.string.hint_getsjauth_fail));
                            } else {
                                boolean isSaveSuccess = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, accessToken);
                                if (!isSaveSuccess) {//没有成功保存token
                                }
                                RestClient.getInstance().setAccessToken(accessToken);
                                view.bindSuccess();
                            }
                        } else {
                            view.sendVCodeFaild(res.message);
                            view.bindFail();
                        }
                    }
                }, e -> {
                    view.dismissProgressDialog();
                    try {
                        RetrofitError retrofitError = (RetrofitError) e;
                        if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.NETWORK.toString())) {
                            view.sendVCodeFaild("网络开小差了");
                        } else {
                            view.sendVCodeFaild(SliceApp.CONTEXT.getString(R.string.server_error));
                        }

                    } catch (IllegalStateException e1) {
                        view.sendVCodeFaild("网络开小差了");

                    }
                    view.bindFail();
                });
    }
}
