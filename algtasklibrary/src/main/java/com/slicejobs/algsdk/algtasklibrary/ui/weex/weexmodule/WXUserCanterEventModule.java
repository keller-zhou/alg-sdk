package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexmodule;

import com.slicejobs.algsdk.algtasklibrary.app.AppEvent;
import com.slicejobs.algsdk.algtasklibrary.utils.BusProvider;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

import java.util.Map;

public class WXUserCanterEventModule extends WXModule {

    @WXModuleAnno
    public void updateMarketCount(Map<String, Object> params) {//更新门店数量
        BusProvider.getInstance().post(new AppEvent.ModifyMarketViewEvent(params));
    }

}
