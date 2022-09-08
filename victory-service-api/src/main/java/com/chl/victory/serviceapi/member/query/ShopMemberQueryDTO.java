package com.chl.victory.serviceapi.member.query;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class ShopMemberQueryDTO extends BaseQuery implements Serializable {
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
    private boolean hasMobile;

}