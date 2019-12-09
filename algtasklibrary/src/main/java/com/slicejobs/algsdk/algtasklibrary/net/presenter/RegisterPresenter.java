package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.LoginRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.RegisterRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IRegisterView;
import com.umeng.analytics.MobclickAgent;

import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 7/12/15.
 */
public class RegisterPresenter extends BasePresenter {

    private IRegisterView view;

    public RegisterPresenter(IRegisterView view) {
        this.view = view;
    }

    public void getVCode(String cellphone) {
        view.showProgressDialog();
        String timestamp = DateUtil.getCurrentTime();

        String sig = new SignUtil.SignBuilder()
                .put("cellphone", cellphone)
                .put("timestamp", timestamp)
                .build();

        restClient.provideApi().getVCode(cellphone, timestamp, sig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    view.dismissProgressDialog();
                    if (res.ret == 0) {
                        view.sendVCodeSuccess();
                    } else {
                        view.sendVCodeFaild(res.msg);
                    }
                }, e -> {
                    view.dismissProgressDialog();
                    try {
                        RetrofitError retrofitError = (RetrofitError) e;
                        if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.NETWORK.toString())) {
                            view.sendVCodeFaild("网络开小差了");
                        } else {
                            MobclickAgent.reportError(SliceApp.CONTEXT, "注册获取验证码：用户手机号：" + cellphone + "报错具体原因：" + e.getMessage());
                            view.sendVCodeFaild(SliceApp.CONTEXT.getString(R.string.server_error));
                        }
                    } catch (IllegalStateException e1) {
                        view.sendVCodeFaild("网络开小差了哦");
                        MobclickAgent.reportError(SliceApp.CONTEXT, "注册获取验证码2：用户手机号" + cellphone + "报错:" +e.toString());
                    }
                });
    }

    public void register(String cellphone, String vcode, String referrer, String referrercode) {
        String timestamp = DateUtil.getCurrentTime();
        view.showProgressDialog();
        Observable<Response<RegisterRes>> registerOb = null;
        SignUtil.SignBuilder signBuilder = new SignUtil.SignBuilder();
        signBuilder.put("cellphone", cellphone).put("vcode", vcode);

        if (StringUtil.isNotBlank(referrer) && StringUtil.isNotBlank(referrercode)) {
            signBuilder.put("referrer", referrer);
            signBuilder.put("referrercode", referrercode);
            signBuilder.put("timestamp", timestamp);
            registerOb = restClient.provideApi().register(cellphone, vcode, referrer, referrercode, timestamp, signBuilder.build());
        } else if (StringUtil.isNotBlank(referrer)) {
            signBuilder.put("referrer", referrer);
            signBuilder.put("timestamp", timestamp);
            registerOb = restClient.provideApi().register(cellphone, vcode, referrer, timestamp, signBuilder.build());
        } else if (StringUtil.isNotBlank(referrercode)) {
            signBuilder.put("referrercode", referrercode);
            signBuilder.put("timestamp", timestamp);
            registerOb = restClient.provideApi().register2(cellphone, vcode, referrercode, timestamp, signBuilder.build());
        } else {
            signBuilder.put("timestamp", timestamp);
            registerOb = restClient.provideApi().register(cellphone, vcode, timestamp, signBuilder.build());
        }

        registerOb.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Response<RegisterRes>>() {
                    @Override
                    public void call(Response<RegisterRes> res) {
                        view.dismissProgressDialog();
                        if (res.ret == 0) {
                            LoginRes loginRes = new LoginRes();
                            loginRes.userid = res.detail.userid;
                            loginRes.cellphone = res.detail.cellphone;
                            loginRes.authkey = res.detail.authkey;

                            RestClient.getInstance().setAccessToken(res.detail.authkey);
                            PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, loginRes);
                            if (StringUtil.isBlank(res.detail.authkey) || res.detail.authkey.length() < 3) {//防止sj没有传递下来
                                MobclickAgent.reportError(SliceApp.CONTEXT, "用户" + res.detail.userid + "注册时传下token 为空");
                            } else {
                                if (PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, res.detail.authkey)) {
                                    BusProvider.getInstance().post(new AppEvent.Register1Event());
                                }
                            }
                        } else {
                            view.sendVCodeFaild(res.msg);
                            view.registerFail();
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
                            MobclickAgent.reportError(SliceApp.CONTEXT, "注册提交注册：用户手机号：" + cellphone + "报错具体原因：" + e.getMessage());
                        }

                    } catch (IllegalStateException e1) {
                        view.sendVCodeFaild("网络开小差了");
                        MobclickAgent.reportError(SliceApp.CONTEXT, "注册提交注册2：用户手机号：" + cellphone + "报错具体原因：" + e.getMessage());

                    }
                    view.registerFail();
                });
    }
}
