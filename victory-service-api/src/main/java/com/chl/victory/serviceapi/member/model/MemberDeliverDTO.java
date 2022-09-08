package com.chl.victory.serviceapi.member.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class MemberDeliverDTO extends BaseDTO implements Serializable {
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

    public String getCity() {
        String city = addr.split("\\|")[0];
        return city;
    }
    public String getAddress() {
        String address = addr.split("\\|")[1];
        return address;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setAddr(String addr) {
        this.addr = addr == null ? null : addr.trim();
    }
    public void setAddr(String city, String address) {
        this.addr = city + "|" + address;
    }
}