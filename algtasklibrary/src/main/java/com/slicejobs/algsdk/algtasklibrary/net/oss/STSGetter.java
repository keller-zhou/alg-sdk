package com.slicejobs.algsdk.algtasklibrary.net.oss;


import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.slicejobs.algsdk.algtasklibrary.app.BizLogic;
import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.model.OSSTicket;
import com.slicejobs.algsdk.algtasklibrary.net.AppConfig;
import com.slicejobs.algsdk.algtasklibrary.net.RestClient;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.utils.DateUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.PrefUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;

/**
 * Created by nlmartian on 12/30/15.
 */
public class STSGetter extends OSSFederationCredentialProvider {
    @Override
    public OSSFederationToken getFederationToken() {
        String timestamp = DateUtil.getCurrentTime();
        String userId = BizLogic.getCurrentUser().userid;
        String appId = PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).getString(AppConfig.ZDD_APPID);
        String sig = new SignUtil.SignBuilder().put("userid", userId).put("ossid", "1").put("timestamp", timestamp).put("appId", appId).build();
        try {
            Response<OSSTicket> response = RestClient.getInstance().provideApi().getOSSTicket(userId, "1" ,timestamp,appId, sig);
            if (response != null && response.detail != null) {
                OSSTicket ossTicket = response.detail;

                PrefUtil.make(SliceApp.CONTEXT, PrefUtil.PREFERENCE_NAME).putObject(AppConfig.OSS_TOKEN_KEY, response.detail);

                return new OSSFederationToken(
                        ossTicket.getAccessKeyId(),
                        ossTicket.getAccessKeySecret(),
                        ossTicket.getSecurityToken(),
                        ossTicket.getExpiration());
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
