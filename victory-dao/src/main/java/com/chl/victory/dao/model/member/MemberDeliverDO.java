package com.chl.victory.dao.model.member;

import com.chl.victory.dao.model.BaseDO;

public class MemberDeliverDO extends BaseDO {
    /**
     * 店铺会员ID
     * mem_id
     */
    private Long memId;

    /**
     * 收货人手机号
     * mobile
     */
    private Long mobile;

    /**
     * 收货人
     * name
     */
    private String name;

    /**
     * 收货地址   city|address, 如xx省 xx市 xx县|xx街道xx号
     * addr
     */
    private String addr;

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        String city = addr.split("\\|")[0];
        return city;
    }
    public String getAddress() {
        String address = addr.split("\\|")[1];
        return address;
    }

    public Long getMemId() {
        return memId;
    }

    public void setMemId(Long memId) {
        this.memId = memId;
    }

    public Long getMobile() {
        return mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr == null ? null : addr.trim();
    }
    public void setAddr(String city, String address) {
        this.addr = city + "|" + address;
    }
}