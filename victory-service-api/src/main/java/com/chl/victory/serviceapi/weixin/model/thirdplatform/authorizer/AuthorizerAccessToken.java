package com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class AuthorizerAccessToken  implements Serializable {

    /**
     * 授权方令牌
     */
    String authorizer_access_token;

    /**
     * 刷新令牌
     */
    String authorizer_refresh_token;

    Long expires_in;

    /**
     * 该token获取的时间，用于判断是否该重新获取
     */
    Date createTime;
}
