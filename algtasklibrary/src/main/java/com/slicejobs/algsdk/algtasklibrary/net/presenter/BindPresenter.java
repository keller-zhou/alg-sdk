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
import com.umeng.analytics.MobclickAgent;

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

    public void getVCode(String cellphone) {
        view.showProgressDialog();
        String timestamp = DateUtil.getCurrentTime();
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        String sig = new SignUtil.SignBuilder()
                .put("cellphone", cellphone)
                .put("timestamp", timestamp)
                .put("appId", appId)
                .build();

        restClient.provideApi().getVCode(cellphone, timestamp,appId ,sig)
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
                                MobclickAgent.reportError(SliceApp.CONTEXT, "用户" + res.data.userid + "登陆时传下token 为空");
                            } else {
                                boolean isSaveSuccess = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, accessToken);
                                if (!isSaveSuccess) {//没有成功保存token
                                    MobclickAgent.reportError(SliceApp.CONTEXT, "用户登录时用户ID"+res.data.userid+"手机型号"+ Build.MANUFACTURER + "-" + Build.MODEL + "没有成功保存token");
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
                            MobclickAgent.reportError(SliceApp.CONTEXT, "注册提交注册：用户手机号：" + mobile + "报错具体原因：" + e.getMessage());
                        }

                    } catch (IllegalStateException e1) {
                        view.sendVCodeFaild("网络开小差了");
                        MobclickAgent.reportError(SliceApp.CONTEXT, "注册提交注册2：用户手机号：" + mobile + "报错具体原因：" + e.getMessage());

                    }
                    view.bindFail();
                });
    }
}
