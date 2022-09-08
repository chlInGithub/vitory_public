package com.chl.victory.dao.model.member;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

@Data
public class ShopMemberDO extends BaseDO {
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