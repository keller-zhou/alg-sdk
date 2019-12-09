package com.slicejobs.algsdk.algtasklibrary.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nlmartian on 12/29/15.
 */
public class OSSTicket {

    @SerializedName("AccessKeySecret")
    private String accessKeySecret;

    @SerializedName("AccessKeyId")
    private String accessKeyId;

    @SerializedName("Expiration")
    private String expiration;

    @SerializedName("SecurityToken")
    private String securityToken;

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}
