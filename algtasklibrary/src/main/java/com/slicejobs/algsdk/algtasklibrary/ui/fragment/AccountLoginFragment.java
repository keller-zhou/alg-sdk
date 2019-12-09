package com.slicejobs.algsdk.algtasklibrary.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.app.SliceStaticStr;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.LoginPresenter;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.MainActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.ModifyPasswordActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseFragment;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ILoginView;
import com.squareup.otto.Subscribe;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AccountLoginFragment extends BaseFragment implements ILoginView {

    private static final int GO_MODIFY_PWD_REQUEST_CODE = 11;
    @BindView(R2.id.et_telephone)
    EditText etPhone;
    @BindView(R2.id.et_password)
    EditText etPassword;

    ViewPager vp_view;
    TabLayout tabs;
    private LoginPresenter presenter;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_login, container, false);
        ButterKnife.bind(this, view);
        presenter = new LoginPresenter(this);
        tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        vp_view = (ViewPager) getActivity().findViewById(R.id.vp_view);
        return view;
    }

    @OnClick({R2.id.btn_login, R2.id.btn_register, R2.id.tv_forget_pwd})
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
            } else if (StringUtil.isBlank(etPassword.getText().toString())) {
                toast(getString(R.string.affirmpassword_notnull));
            } else {
                AccountLoginFragmentPermissionsDispatcher.userLoginWithCheck(this);
            }
        } else if (view.getId() == R.id.btn_register) {
            vp_view.setCurrentItem(1);
            tabs.getTabAt(1).select();
        } else if (view.getId() == R.id.tv_forget_pwd) {
            Intent toModifyPwd = ModifyPasswordActivity.getStartIntent(getActivity(), ModifyPasswordActivity.OPEN_SOURCE_LOGIN);
            startActivityForResult(toModifyPwd, GO_MODIFY_PWD_REQUEST_CODE);


        }
    }

    @Override
    public void loginSuccess() {
        MobclickAgent.onEvent(getActivity(), "um_function_pw_login_click");

        if (etPassword.getText().toString().length() < 7 &&  StringUtil.isSamplePassword(etPassword.getText().toString())) {
            showHintDialog(new BaseFragment.DialogClickLinear() {
                @Override
                public void cancelClick() {
                    startActivity(ModifyPasswordActivity.getStartIntent(getActivity(), ModifyPasswordActivity.OPEN_SOURCE_LOGIN));
                }

                @Override
                public void defineClick() {

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            }, getString(R.string.text_slicejobs_hint), "您当前的登录密码太过简单，建议修改", "修改密码", "忽略", false);
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void serverExecption(String source) {
        if (null != presenter && StringUtil.isMobilePhone(etPhone.getText().toString())) {
            presenter.login(etPhone.getText().toString(), etPassword.getText().toString());
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

    /**
     * android6.0以上部分手机，无状态管理的权限时，
     */
    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void userLogin() {
        presenter.login(etPhone.getText().toString(), etPassword.getText().toString());
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

        AccountLoginFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }

    public String getInputPhoneNumber() {
        return etPhone.getText().toString();
    }

    public void setInputPhoneNumber (String phoneNumber) {
        etPhone.setText(phoneNumber);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GO_MODIFY_PWD_REQUEST_CODE) {
                String modifyPwdPhone = data.getStringExtra(ModifyPasswordActivity.MODIFY_PWD_PHONE);
                if(StringUtil.isNotBlank(modifyPwdPhone)) {
                    etPhone.setText(modifyPwdPhone);
                }
            }
        }
    }
}
