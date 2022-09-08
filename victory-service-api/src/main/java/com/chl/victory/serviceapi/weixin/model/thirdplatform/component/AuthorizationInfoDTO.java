package com.chl.victory.serviceapi.weixin.model.thirdplatform.component;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.common.FuncInfo;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 15:55
 **/
@Data
public class AuthorizationInfoDTO extends BaseResult  implements Serializable {

    /**
     * 授权方 appid
     */
    @NotEmpty
    String authorizer_appid;

    /**
     * 接口调用令牌（在授权的公众号/小程序具备 API 权限时，才有此返回值）
     */
    @NotEmpty
    String authorizer_access_token;

    /**
     * authorizer_access_token 的有效期（在授权的公众号/小程序具备API权限时，才有此返回值），单位：秒
     */
    Integer expires_in;

    /**
     * 刷新令牌（在授权的公众号具备API权限时，才有此返回值），刷新令牌主要用于第三方平台获取和刷新已授权用户的 authorizer_access_token。一旦丢失，只能让用户重新授权，才能再次拿到新的刷新令牌。用户重新授权后，之前的刷新令牌会失效
     */
    @NotEmpty
    String authorizer_refresh_token;

    /**
     * 授权给开发者的权限集列表
     */
    FuncInfo[] func_info;
}
