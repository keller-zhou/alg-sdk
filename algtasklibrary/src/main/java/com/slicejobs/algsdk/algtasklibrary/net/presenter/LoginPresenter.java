package com.slicejobs.algsdk.algtasklibrary.net.presenter;


import android.os.Build;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.LoginRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ILoginView;
import com.umeng.analytics.MobclickAgent;

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

    public void login(String cellphone, String password) {
        loginView.showProgressDialog();
        String timestamp = DateUtil.getCurrentTime();
        String sig = new SignUtil.SignBuilder()
                .put("cellphone", cellphone)
                .put("cellphonetype", "10")
                .put("password", password)
                .put("timestamp", timestamp)
                .build();

        restClient.provideApi().login(cellphone, "10", password, timestamp, sig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Response<LoginRes>>() {
                    @Override
                    public void call(Response<LoginRes> res) {
                        loginView.dismissProgressDialog();
                        if (res.ret == 0) {
                            PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, res.detail);
                            String accessToken = res.detail.authkey;
                            if (StringUtil.isBlank(accessToken) || accessToken.length() < 3) {//防止sj没有传递下来
                                loginView.toast(SliceApp.CONTEXT.getString(R.string.hint_getsjauth_fail));
                                MobclickAgent.reportError(SliceApp.CONTEXT, "用户" + res.detail.userid + "登陆时传下token 为空");
                            } else {
                                boolean isSaveSuccess = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, accessToken);
                                if (!isSaveSuccess) {//没有成功保存token
                                    MobclickAgent.reportError(SliceApp.CONTEXT, "用户登录时用户ID"+res.detail.userid+"手机型号"+ Build.MANUFACTURER + "-" + Build.MODEL + "没有成功保存token");
                                }
                                RestClient.getInstance().setAccessToken(accessToken);
                                loginView.loginSuccess();
                            }
                        } else if (res.ret == 4) {
                            loginView.toast(SliceApp.CONTEXT.getString(R.string.cellphone_format_err));
                        } else {
                            loginView.toast(res.msg);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        loginView.dismissProgressDialog();

                        try {
                            RetrofitError retrofitError = (RetrofitError) e;
                            if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.NETWORK.toString())) {
                                MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone +"报错:"+retrofitError.getKind().toString()+ "具体原因：" + retrofitError.getMessage());
                                loginView.toast("网络开小差了");
                            } else if (StringUtil.isNotBlank(retrofitError.getKind().toString()) && retrofitError.getKind().toString().equals(RetrofitError.Kind.CONVERSION.toString())) {
                                MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone +"报错:"+retrofitError.getKind().toString()+ "具体原因：" + retrofitError.getMessage());
                                loginView.toast("帐号或密码输入错误");
                            } else {
                                MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone +"报错:"+retrofitError.getKind().toString()+ "具体原因：" + retrofitError.getMessage());
                                loginView.toast("服务器网络开小差");
                            }
                        } catch (IllegalStateException e1) {
                            loginView.toast("网络开小差了哦");
                            MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone + "报错:" +e.toString());
                        }
                    }
                });
    }

    /*
    * 验证码登录
    * */
    public void vCodeLogin(String cellphone, String vcode) {
        loginView.showProgressDialog();
        String timestamp = DateUtil.getCurrentTime();
        String sig = new SignUtil.SignBuilder()
                .put("logintype", "1")
                .put("cellphone", cellphone)
                .put("vcode", vcode)
                .put("timestamp", timestamp)
                .build();

        restClient.checkoutChannel();
        restClient.provideApi().vCodeLogin("1",cellphone, vcode, timestamp, sig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Response<LoginRes>>() {
                    @Override
                    public void call(Response<LoginRes> res) {
                        loginView.dismissProgressDialog();
                        if (res.ret == 0) {
                            PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, res.detail);
                            String accessToken = res.detail.authkey;
                            if (StringUtil.isBlank(accessToken) || accessToken.length() < 3) {//防止sj没有传递下来
                                loginView.toast(SliceApp.CONTEXT.getString(R.string.hint_getsjauth_fail));
                                MobclickAgent.reportError(SliceApp.CONTEXT, "用户" + res.detail.userid + "登陆时传下token 为空");
                            } else {
                                boolean isSaveSuccess = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, accessToken);
                                if (!isSaveSuccess) {//没有成功保存token
                                    MobclickAgent.reportError(SliceApp.CONTEXT, "用户登录时用户ID"+res.detail.userid+"手机型号"+ Build.MANUFACTURER + "-" + Build.MODEL + "没有成功保存token");
                                }
                                RestClient.getInstance().setAccessToken(accessToken);
                                loginView.loginSuccess();
                            }
                        } else if (res.ret == 4) {
                            loginView.toast(SliceApp.CONTEXT.getString(R.string.cellphone_format_err));
                        } else {
                            loginView.toast(res.msg);
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
                            } else {
                                MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone +"报错:"+retrofitError.getKind().toString()+ "具体原因：" + retrofitError.getMessage());
                                loginView.toast("服务器网络开小差");
                            }
                        } catch (IllegalStateException e1) {
                            loginView.toast("网络开小差了哦");
                            MobclickAgent.reportError(SliceApp.CONTEXT, "登录不上：用户手机号：" + cellphone + "报错:" +e.toString());
                        }
                    }
                });
    }
}
