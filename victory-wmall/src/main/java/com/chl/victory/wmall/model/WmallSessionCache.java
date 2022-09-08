package com.chl.victory.wmall.model;

import com.chl.victory.webcommon.model.SessionCache;
import lombok.Data;

/**
 * 自定义session
 *
 * @author ChenHailong
 * @date 2019/4/23 14:12
 **/
@Data
public class WmallSessionCache extends SessionCache {
    /**
     * 第三方平台标识
     */
    String thirdId;
    /**
     * 第三方用户名称
     */
    String thirdNick;
    /**
     * 第三方用户头像地址
     */
    String thirdImg;
    /**
     * 第三方用户唯一标识，如微信用户唯一标识
     */
    String thirdOpenId;

    String sessionKey;

    /**
     * 域名
     */
    String rootDomain;
    String thirdCode;

    boolean hasFilled;

    String token;

    /**
     * 是否店铺管理
     */
    boolean isShopMan;

    /**
     * 店铺管理： 店铺管理人员ID merchant_user.id
     */
    Long userId4ShopMan;

    /**
     * 店铺管理： 店铺ID
     */
    Long shopId4ShopMan;
}
