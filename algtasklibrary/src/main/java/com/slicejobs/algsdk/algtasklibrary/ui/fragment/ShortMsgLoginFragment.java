package com.slicejobs.algsdk.algtasklibrary.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.LoginPresenter;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.RegisterPresenter;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.MainActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.WebviewActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ILoginView;
import com.slicejobs.algsdk.algtasklibrary.view.IRegisterView;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

@RuntimePermissions
public class ShortMsgLoginFragment extends BaseFragment implements ILoginView,IRegisterView {

    @BindView(R2.id.et_telephone)
    EditText etPhone;
    @BindView(R2.id.et_vcode)
    EditText etVCode;
    @BindView(R2.id.tv_get_vcode)
    TextView tvGetVcode;
    @BindView(R2.id.register_notice)
    TextView registerNotice;
    private LoginPresenter presenter;
    private RegisterPresenter registerPresenter;
    private Subscription sub;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (sub != null && !sub.isUnsubscribed()) {
            sub.unsubscribe();
        }
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shortmsg_login, container, false);
        ButterKnife.bind(this, view);
        presenter = new LoginPresenter(this);
        registerPresenter = new RegisterPresenter(this);
        setRegisterNotice();
        return view;
    }

    @OnClick({R2.id.btn_login, R2.id.tv_get_vcode,R2.id.tv_contact_service})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_login) {
            if(StringUtil.isBlank(etPhone.getText().toString())){
                toast(getString(R.string.telephone_number));
                return;
            }
            //删除活动配置
            deleteActConfig();

            if (!StringUtil.isMobilePhone(etPhone.getText().toString())) {
                toast(getString(R.string.cellphone_format_err));
            } else if (StringUtil.isBlank(etVCode.getText().toString())) {
                toast(getString(R.string.vcode_empty));
            } else {
                ShortMsgLoginFragmentPermissionsDispatcher.userLoginWithCheck(this);
            }
        } else if (view.getId() == R.id.tv_get_vcode) {
            if(StringUtil.isBlank(etPhone.getText().toString())){
                toast(getString(R.string.telephone_number));
                return;
            }
            if (StringUtil.isMobilePhone(etPhone.getText().toString())) {
                registerPresenter.getVCode(etPhone.getText().toString());
            } else {
                toast(getString(R.string.cellphone_format_err));
            }
        }  else if (view.getId() == R.id.tv_contact_service) {
            showHintDialog(new BaseFragment.DialogClickLinear() {
                               @Override
                               public void cancelClick() {

                               }

                               @Override
                               public void defineClick() {
                                   Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:400-808-1032"));
                                   startActivity(intent);
                               }
                           }, SliceApp.CONTEXT.getString(R.string.if_call_service_phone), "400-808-1032",
                    SliceApp.CONTEXT.getString(R.string.cancel), SliceApp.CONTEXT.getString(R.string.confirm), false);
        }
    }

    private void deleteActConfig() {
        //删除活动配置
        try {
            SharedPreferences sp = getActivity().getSharedPreferences(AppConfig.ACT_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        } catch (Exception e) {

        }
    }

    private void setRegisterNotice(){
        String str = "未注册爱零工的手机号，登录时将自动注册并代表您已经同意《爱零工注册协议》";
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        //文字颜色
        ForegroundColorSpan blueColor = new ForegroundColorSpan(getActivity().getResources().getColor(R.color.blue_text_color));
        builder.setSpan(blueColor, 27, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerNotice.setText(builder);

        //设置点击事件....
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                getActivity().startActivity(WebviewActivity.getStartIntent(getActivity(),AppConfig.webHost.getAppWebHost()+"/public/user_term.html"));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getActivity().getResources().getColor(R.color.blue_text_color));
                //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
                ds.setUnderlineText(false);
            }
        }, 27, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerNotice.setText(spannableString);
        registerNotice.setMovementMethod(LinkMovementMethod.getInstance());
        registerNotice.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
    }

    @Override
    public void sendVCodeSuccess() {
        toast(getString(R.string.msg_send_success));
        colddownVCode();
    }

    @Override
    public void registerFail() {

    }

    @Override
    public void sendVCodeFaild(String msg) {//验证码发送失败
        showHintDialog(new DialogDefineClick() {
            @Override
            public void defineClick() {

            }
        }, SliceApp.CONTEXT.getString(R.string.text_slicejobs_hint), msg, "我知道了", true);
    }

    @Override
    public void loginSuccess() {
        MobclickAgent.onEvent(getActivity(), "um_function_sms_login_click");
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void serverExecption(String source) {
        if (null != presenter && StringUtil.isMobilePhone(etPhone.getText().toString())) {
            presenter.login(etPhone.getText().toString(), etVCode.getText().toString());
        }
    }

    /**
     * android6.0以上部分手机，无状态管理的权限时，
     */
    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void userLogin() {
        presenter.vCodeLogin(etPhone.getText().toString(), etVCode.getText().toString());
    }

    //用户点击不再询问这个权限时调用（实际上用户禁止权限会调用）这个方法执行在非UI线程
    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onChatNeverAskAgain() {

        showHintDialog(new BaseFragment.DialogClickLinear() {
            @Override
            public void cancelClick() {
                userLogin();
            }

            @Override
            public void defineClick() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        }, "", "请在手机“设置-应用程序权限管理-选择爱零工”允许（获取手机基本信息，和读写手机存储）", "以后再说", "打开", false);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ShortMsgLoginFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }

    private void colddownVCode() {
        tvGetVcode.setEnabled(false);
        tvGetVcode.setText(getString(R.string.vcode_send_again) + "(" + AppConfig.VCODE_WAIT_TIME + ")");
        sub = Observable.zip(Observable.timer(1, 1, TimeUnit.SECONDS),
                Observable.range(1, AppConfig.VCODE_WAIT_TIME),
                (aLong, integer) -> AppConfig.VCODE_WAIT_TIME - integer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == 0) {
                        tvGetVcode.setEnabled(true);
                        tvGetVcode.setText(R.string.vcode_send_again);
                    } else {
                        tvGetVcode.setText(getString(R.string.vcode_send_again) + "(" + integer + ")");
                    }
                }, e -> {});
    }

    public String getInputPhoneNumber() {
        return etPhone.getText().toString();
    }

    public void setInputPhoneNumber (String phoneNumber) {
        etPhone.setText(phoneNumber);
    }
}
