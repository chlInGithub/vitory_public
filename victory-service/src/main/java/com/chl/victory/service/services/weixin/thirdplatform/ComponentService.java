package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.AuthorizationInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.AuthorizationInfoResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.ComponentAccessToken;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.GetTemplatesResult;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;

/**
 * 第三方平台相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.component")
@Slf4j
public class ComponentService {

    final int weixinAccessTokenTimeoutSecounds = 60 * 60 * 2;

    @Setter
    private ComponentConfig config;

    @Setter
    private PayComponentConfig payConfig;

    @Setter
    private String componentAppSecret;

    @Setter
    private String apiComponentToken;

    @Setter
    private String apiCreatePreauthcode;

    @Setter
    private String apiQueryAuth;

    @Setter
    private String gettemplatelist;

    @Setter
    private String componentloginpageRedirectUri;

    @PostConstruct
    public void PostConstruct() {
        if (null == config) {
            throw new Error("component config null");
        }

        ValidationUtil.validate(config);
    }

    public ComponentConfig getComponentConfig() {
        return config;
    }

    public PayComponentConfig getPayComponentConfig() {
        return payConfig;
    }

    public void assertComponentId(String componentId) {
        if (!config.componentAppId.equals(componentId)) {
            throw new ThirdPlatformServiceException("第三方平台标识错误");
        }
    }

    /**
     * 获取授权注册页面URL ，让客户扫码授权
     * @return
     */
    public String getComponentLoginPage(@NotEmpty String preAuthCode) {
        String componentAppId = config.getComponentAppId();
        Integer authType = 2;
        String url = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=" + componentAppId
                + "&pre_auth_code=" + preAuthCode + "&redirect_uri=" + componentloginpageRedirectUri + "&auth_type="
                + authType;
        return url;
    }

    /**
     * 获取代码模板列表
     * @return
     */
    public GetTemplatesResult getTemplates() {
        Map<String, String> vars = new HashMap<>();
        vars.put("access_token", getComponentAccessToken().getComponent_access_token());

        GetTemplatesResult post = httpClientService.post(gettemplatelist, null, GetTemplatesResult.class, vars);

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        return post;
    }

    /**
     * 更新第三方平台的验证票据
     * @param appId 第三方平台 appid
     * @param ticket Ticket 内容
     * @link https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/api/component_verify_ticket.html
     */
    public void updateComponentVerifyTicket(@NotEmpty String appId, @NotEmpty String ticket) {
        // check
        if (!config.componentAppId.equals(appId)) {
            throw new ThirdPlatformServiceException("参数appId与当前第三方平台appId不匹配");
        }

        // cache
        String key = CacheKeyPrefix.THIRD_PLATFORM_VERIFY_TICKET;
        ServiceManager.cacheService.save(key, ticket);

        // TODO db
    }

    private String getComponentVerifyTicket() {
        // cache
        String key = CacheKeyPrefix.THIRD_PLATFORM_VERIFY_TICKET;
        String ticket = ServiceManager.cacheService.get(key);

        // TODO db

        if (StringUtils.isBlank(ticket)) {
            throw new ThirdPlatformServiceException("缺失第三方平台验证票据");
        }
        return ticket;
    }

    public ComponentAccessToken getComponentAccessToken() {
        ComponentAccessToken weixinAccessToken = getComponentAccessTokenCache();

        if (null == weixinAccessToken) {
            getNewComponentAccessToken();

            weixinAccessToken = getComponentAccessTokenCache();
        }

        return weixinAccessToken;
    }

    /**
     * 第三方平台接口的调用凭据
     * @return
     * @link https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/api/component_access_token.html
     */
    private ComponentAccessToken getNewComponentAccessToken() {
        String nxLockKey = CacheKeyPrefix.WEIXIN_ACCESSTOKEN + config.componentAppId;
        String nxLockRandom = cacheService.getNXLock(nxLockKey, 1L);

        ComponentAccessToken weixinAccessToken = null;
        if (null != nxLockRandom) {
            // 重新获取并更新cache
            try {
                weixinAccessToken = getNewComponentAccessTokenFromWeixin();
                if (StringUtils.isNotBlank(weixinAccessToken.getComponent_access_token())) {
                    setComponentAccessTokenCache(weixinAccessToken);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                cacheService.releaseNXLock(nxLockKey, nxLockRandom);
            }
        }
        else {
            // 间隔等待一段时间，判断排它锁是否已经消失
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                boolean existsKey = cacheService.existsKey(nxLockKey);
                if (!existsKey) {
                    weixinAccessToken = getComponentAccessTokenCache();
                    break;
                }
            }
        }

        return weixinAccessToken;
    }

    private ComponentAccessToken getNewComponentAccessTokenFromWeixin() {
        String componentVerifyTicket = getComponentVerifyTicket();

        Map<String, Object> vars = new HashMap<>();
        vars.put("component_verify_ticket", componentVerifyTicket);
        vars.put("component_appid", config.componentAppId);
        vars.put("component_appsecret", componentAppSecret);

        String request = JSONObject.toJSONString(vars);
        ComponentAccessToken weixinAccessToken = httpClientService
                .post(apiComponentToken, request, ComponentAccessToken.class);

        if (weixinAccessToken == null || StringUtils.isBlank(weixinAccessToken.getComponent_access_token())) {
            throw new ThirdPlatformServiceException("获取第三方平台令牌无结果" + weixinAccessToken.getError());
        }

        weixinAccessToken.setCreateTime(new Date());
        return weixinAccessToken;
    }

    private ComponentAccessToken getComponentAccessTokenCache() {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = config.componentAppId;
        ComponentAccessToken weixinAccessToken = cacheService.hGet(key, field, ComponentAccessToken.class);
        return weixinAccessToken;
    }

    private void setComponentAccessTokenCache(ComponentAccessToken weixinAccessToken) {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = config.componentAppId;
        cacheService.hSet(key, field, weixinAccessToken);
        cacheService.expire(key, weixinAccessTokenTimeoutSecounds);
    }

    /**
     * 预授权码
     */
    public String createPreAuthCode(@NotNull Long shopId) {
        Map<String, String> vars = new HashMap<>();
        vars.put("component_appid", config.componentAppId);
        String request = JSONObject.toJSONString(vars);

        vars = new HashMap();
        vars.put("component_access_token", getComponentAccessToken().getComponent_access_token());

        String post = ServiceManager.httpClientService.post(apiCreatePreauthcode, request, String.class, vars);

        if (StringUtils.isBlank(post)) {
            throw new ThirdPlatformServiceException("获取预授权码无结果");
        }

        String pre_auth_code = JSONObject.parseObject(post).getString("pre_auth_code");
        if (StringUtils.isBlank(pre_auth_code)) {
            throw new ThirdPlatformServiceException("获取预授权码无结果" + post);
        }

        setPreAuthCodeCache(pre_auth_code, shopId);

        return pre_auth_code;
    }

    void setPreAuthCodeCache(String preAuthCode, Long shopId) {
        String key = CacheKeyPrefix.WEIXIN_AUTH_PRECODE;
        String field = preAuthCode;
        String val = shopId.toString();
        cacheService.hSet(key, field, val, CacheExpire.MINUTE_1 * 10);
    }

    public Long getPreAuthCodeCache(String preAuthCode) {
        String key = CacheKeyPrefix.WEIXIN_AUTH_PRECODE;
        String field = preAuthCode;
        Long shopId = cacheService.hGet(key, field, Long.class);
        return shopId;
    }

    /**
     * 使用授权码获取授权信息
     * @param authorizationCode
     * @return
     */
    public AuthorizationInfoDTO queryAuth(String authorizationCode) {
        Map<String, String> vars = new HashMap<>();
        vars.put("component_appid", config.componentAppId);
        vars.put("authorization_code", authorizationCode);
        String request = JSONObject.toJSONString(vars);

        vars = new HashMap();
        vars.put("component_access_token", getComponentAccessToken().getComponent_access_token());

        AuthorizationInfoResult post = ServiceManager.httpClientService
                .post(apiQueryAuth, request, AuthorizationInfoResult.class, vars);

        AuthorizationInfoDTO authorization_info = post.getAuthorization_info();
        try {
            ValidationUtil.validate(authorization_info);
        } catch (Exception e) {
            throw new ThirdPlatformServiceException("授权码无有效授权信息," + post.getError(), e);
        }

        return authorization_info;
    }

    @Data
    public static class ComponentConfig {

        @NotEmpty String componentAppId;

        @NotEmpty String encodingAesKey;

        @NotEmpty String token;

        @NotEmpty String[] webviewDomain;

        @NotEmpty String requestDomain;
    }

    @Data
    public static class PayComponentConfig {

        /**
         * 服务商的APPID
         */
        @NotEmpty String appId;

        /**
         * 商户号
         */
        @NotEmpty String mchId;

        @NotEmpty String apiKey;
    }

}
