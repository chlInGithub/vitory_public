package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * 第三方平台 代小程序 微信登录相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.login")
@Slf4j
public class FoundryMiniProgram4WeixinLoginService {
    @Resource
    ComponentService thirdPlatformComponentService;

    @Setter
    String jscode2session;

    /**
     * 为授权appid，解析微信用户登录信息
     * @param weixinConfig
     * @param code
     * @return
     */
    public WeixinCode2Session getSession(@NotNull ShopAppDO weixinConfig, String code) {
        Map<String, String> vars = new HashMap<>();
        vars.put("appid", weixinConfig.getAppId());
        vars.put("js_code", code);
        vars.put("component_appid", thirdPlatformComponentService.getComponentConfig().getComponentAppId());
        vars.put("component_access_token", thirdPlatformComponentService.getComponentAccessToken().getComponent_access_token());

        String result = ServiceManager.httpClientService
                .post(jscode2session, null, String.class, vars);
        WeixinCode2Session post = JSONObject.parseObject(result, WeixinCode2Session.class);

        if (StringUtils.isBlank(post.getOpenid())) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        return post;
    }
}
