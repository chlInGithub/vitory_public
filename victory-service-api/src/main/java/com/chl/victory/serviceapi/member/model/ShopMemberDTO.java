package com.chl.victory.serviceapi.member.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopMemberDTO extends BaseDTO implements Serializable {
    /**
     * 第三方用户标识
     */
    private String thirdId;
    /**
     * 手机号
     * mobile
     */
    private Long mobile;

    /**
     * 昵称
     * nick
     */
    private String nick;

    private String appId;

    private String openId;

    String avatarUrl;

    public void setNick(String nick) {
        this.nick = nick == null ? null : nick.trim();
    }

}