package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.User;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.IJsRenderListener;
import com.squareup.otto.Subscribe;
import com.taobao.weex.WXSDKInstance;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;


public class ModifyPasswordActivity extends BaseActivity implements IJsRenderListener {

    public static final String OPEN_SOURCE = "openSource";

    public static final String OPEN_SOURCE_LOGIN = "login";

    public static final String OPEN_SOURCE_SETTING = "setting";

    public static final String MODIFY_PWD_PHONE = "modify_pwd_phone";
    @BindView(R2.id.modify_pw_view)
    RelativeLayout modifyPwView;
    WXSDKInstance mWXSDKInstance;

    public static Intent getStartIntent(Context context, String openSource) {
        Intent intent = new Intent(context, ModifyPasswordActivity.class);
        intent.putExtra(OPEN_SOURCE, openSource);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        renderJs(AppConfig.MODIFY_PASSWORD_VIEW_FILE,null,"修改密码",this);
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        this.mWXSDKInstance = instance;
        modifyPwView.addView(view);
    }

    @OnClick({R2.id.action_go_back,R2.id.action_contact})
    public void onClick(View view) {
        if (view.getId() == R.id.action_go_back) {
            finish();
        } else if (view.getId() == R.id.action_contact) {
            showHintDialog(new DialogClickLinear() {
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


    /**
     * 由h5触发接口
     * @param event
     */
    @Subscribe
    public void onModifyPwViewEvent(AppEvent.ModifyPwViewEvent event) {
        if (StringUtil.isBlank(event.eventType)) {
            return;
        } else if (event.eventType.equals("finish")) {
            Gson mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
            User user = mGson.fromJson(event.params.get("detail").toString(), User.class);
            PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.PREF_USER, user);
            RestClient.getInstance().setAccessToken(user.authkey);
            PrefUtil.make(this, PrefUtil.PREFERENCE_NAME).putSaveToken(AppConfig.AUTH_KEY, user.authkey);
            if (event.params.get("phone") != null) {
                Intent intent = new Intent();
                intent.putExtra(MODIFY_PWD_PHONE, event.params.get("phone").toString());
                setResult(RESULT_OK, intent);
            }
            finish();
        }
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

}