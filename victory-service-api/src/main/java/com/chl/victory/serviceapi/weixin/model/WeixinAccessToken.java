package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class WeixinAccessToken extends BaseResult implements Serializable {
    String access_token;
    Integer expires_in;

    /**
     * 该token获取的时间，用于判断是否该重新获取
     */
    Date createTime;
}
