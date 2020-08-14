package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import android.os.Build;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.LoginRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.net.response.ZddResponse;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ILoginView;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 7/9/15.
 */
public class LoginPresenter extends BasePresenter {

    private ILoginView loginView;

    public LoginPresenter(ILoginView view) {
        loginView = view;
    }

    public void login(String appId,String userId,String mobile,String actionTime,String sign) {
        loginView.showProgressDialog();

        restClient.provideOpenApi().login(appId, userId, mobile, actionTime, sign)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ZddResponse<LoginRes>>() {
                    @Override
                    public void call(ZddResponse<LoginRes> res) {
                        loginView.dismissProgressDialog();
                        if (res.code == 0) {
                            PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, res.data);
                            String accessToken = res.data.authkey;
                            if (StringUtil.isBlank(accessToken) || accessToken.length() < 3) {//防止sj没有传递下来
                                loginView.toast(SliceApp.CONTEXT.getString(R.string.hint_getsjauth_fail));
                            } else {
                                boolean isSaveSuccess = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, accessToken);
                                if (!isSaveSuccess) {//没有成功保存token
                                }
                                RestClient.getInstance().setAccessToken(accessToken);
                                loginView.loginSuccess();
                            }
                        } else if (res.code == 160010) {//该手机尚未在爱零工注册，需要绑定
                            loginView.notRegister();
                        } else if (res.code == 4) {
                            loginView.toast(SliceApp.CONTEXT.getString(R.string.cellphone_format_err));
                        } else {
                            loginView.toast(res.message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        loginView.dismissProgressDialog();

                        try {
                            RetrofitError retrofitError = (RetrofitError) e;
                            if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.NETWORK.toString())) {
                                loginView.toast("网络开小差了");
                            } else if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.CONVERSION.toString())) {
                                loginView.toast("帐号或密码输入错误");
                            } else {
                                loginView.toast("服务器网络开小差");
                            }
                        } catch (IllegalStateException e1) {
                            loginView.toast("网络开小差了哦");
                        }
                    }
                });
    }
}
