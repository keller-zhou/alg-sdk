package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;

/**
 * Created by nlmartian on 7/22/15.
 */
public class User implements Serializable {
    public String userid;
    public String realname;
    public String jobid;
    public String marketid;
    public String creditcardno;
    public String bankname;
    public String bankcity;
    public String idnum;
    public String status;
    public String cellphone;
    public String authkey;
    public String photopath;
    public String avatarpath;
    public String balance;
    public String credit;
    public String type;
    public String totalwithdraw;
    public String frozenbalance;//正在提现
    public String nickname;
    public String idcardphotopath;
    public String level;
    public String points;//用户积分
    public String frozenpoints;
    public String spendpoints;
    private Market marketinfo;
    public String idnumverified;
    public String bankcardverified;
    public String nextlevelpoints;
    public String nextlevelpercent;
    public String cityname;
    public String provincename;
    public String areaname;
    public String zfb_account;
    public boolean hasExclusiveTag;
    public String zfb_accountverified;
    public boolean hasPassword;
    public String registertime;
    public String referrer;
    public String idcardphoto_back;

    public Market getMarketinfo() {
        if (marketinfo == null) {
            return new Market();
        }
        return marketinfo;
    }

    public void setMarketinfo(Market marketinfo) {
        this.marketinfo = marketinfo;
    }
}
