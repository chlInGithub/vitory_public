package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;

/**
 * @author ChenHailong
 * @date 2020/5/28 10:07
 **/
public class FoundryMiniProgramBaseService {
    @Resource
    AuthorizerAccessTokenService thirdPlatformAuthorizerAccessTokenService;

    Map<String, String> getAccessTokenVars(ShopAppDO weixinConfig) throws BusServiceException {
        Map<String, String> vars = new HashMap<>();
        vars.put("access_token",
                thirdPlatformAuthorizerAccessTokenService.getAccessToken(weixinConfig).getAuthorizer_access_token());
        return vars;
    }
}
