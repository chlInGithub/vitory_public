package com.chl.victory.webcommon.model;

import lombok.Data;

/**
 * 自定义session
 *
 * @author ChenHailong
 * @date 2019/4/23 14:12
 **/
@Data
public class SessionCache {
    String key;

    /**
     * 小程序appId
     */
    String appId;
    String mobile;
    Long userId;
    Long shopId;
    String shopName;
    String shopImg;
    String shopAddress;
    String shopMobile;
    Long invalidTime;
    /**
     * 毫秒
     */
    Long modifiedTime;

}
