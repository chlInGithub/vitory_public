package com.chl.victory.dao.query.member;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class ShopMemberQuery extends BaseQuery {
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