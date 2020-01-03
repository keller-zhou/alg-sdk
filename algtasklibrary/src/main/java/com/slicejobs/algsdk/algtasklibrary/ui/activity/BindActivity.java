package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.presenter.BindPresenter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule.WXBaseEventModule;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IBindView;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class BindActivity extends BaseActivity implements IBindView{

    @BindView(R2.id.et_vcode)
    EditText etVCode;
    @BindView(R2.id.tv_get_vcode)
    TextView getVcode;
    private BindPresenter bindPresenter;
    private String mobile;
    private Subscription sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        ButterKnife.bind(this);
        bindPresenter = new BindPresenter(this);
        mobile = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_MOBILE, "");
    }

    @OnClick({R2.id.action_go_back,R2.id.btn_bind, R2.id.tv_get_vcode})
    public void onClick(View view) {
        if (view.getId() == R.id.action_go_back) {
            finish();
        }if (view.getId() == R.id.btn_bind) {
            if (StringUtil.isBlank(etVCode.getText().toString())) {
                toast(getString(R.string.vcode_empty));
            } else {
                String appId = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID, "");
                String userId = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_USERID, "");
                String actionTime = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_ACTIONTIME, "");
                String sign = PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_SIGN, "");
                bindPresenter.bind(appId, userId, mobile, actionTime, sign, etVCode.getText().toString());
            }
        } else if (view.getId() == R.id.tv_get_vcode) {
            bindPresenter.getVCode(mobile);
            colddownVCode();
        }
    }

    @Override
    public void sendVCodeSuccess() {
        ToastUtils.show("验证码发送成功");
    }

    @Override
    public void sendVCodeFaild(String msg) {
        ToastUtils.show(msg);
    }

    @Override
    public void bindFail() {

    }

    @Override
    public void bindSuccess() {
        BindActivity.this.finish();
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void toast(String msg) {
        ToastUtils.show(msg);
    }


    private void colddownVCode() {
        getVcode.setEnabled(false);
        getVcode.setText(getString(R.string.vcode_send_again) + "(" + AppConfig.VCODE_WAIT_TIME + ")");
        sub = Observable.zip(Observable.timer(1, 1, TimeUnit.SECONDS),
                Observable.range(1, AppConfig.VCODE_WAIT_TIME),
                (aLong, integer) -> AppConfig.VCODE_WAIT_TIME - integer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == 0) {
                        getVcode.setEnabled(true);
                        getVcode.setText(R.string.vcode_send_again);
                    } else {
                        getVcode.setText(getString(R.string.vcode_send_again) + "(" + integer + ")");
                    }
                }, e -> {});
    }
}
