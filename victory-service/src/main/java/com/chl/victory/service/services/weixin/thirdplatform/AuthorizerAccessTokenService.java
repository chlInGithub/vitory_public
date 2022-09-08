package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.AuthorizerAccessToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.componentService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;

/**
 * 第三方平台 授权者令牌相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.authorizer")
@Slf4j
public class AuthorizerAccessTokenService {

    final int weixinAccessTokenTimeoutSecounds = 60 * 60 * 2;

    @Setter
    String apiAuthorizerToken;

    /**
     * 获取授权appid的接口访问令牌
     */
    public AuthorizerAccessToken getAccessToken(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        AuthorizerAccessToken weixinAccessToken = null;
        weixinAccessToken = getAccessTokenCache(weixinConfig);

        if (null == weixinAccessToken) {
            getNewAccessToken(weixinConfig);

            weixinAccessToken = getAccessTokenCache(weixinConfig);
        }

        if (null == weixinAccessToken) {
            throw new BusServiceException("缺少访问令牌，请稍后再试。");
        }

        return weixinAccessToken;
    }

    private AuthorizerAccessToken getAccessTokenCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = weixinConfig.getAppId();
        AuthorizerAccessToken weixinAccessToken = cacheService.hGet(key, field, AuthorizerAccessToken.class);
        return weixinAccessToken;
    }

    /**
     * 更新店铺 授权appid的访问令牌
     * @param shopIds 限定shop范围，empty or null 则所有店铺。
     */
    public void refreshAccessToken(List<Long> shopIds) {
        // TODO 查询所有店铺与关联的appid和secret

        // TODO 遍历更新
    }

    private AuthorizerAccessToken getNewAccessToken(ShopAppDO weixinConfig) {
        String nxLockKey = CacheKeyPrefix.WEIXIN_ACCESSTOKEN + weixinConfig.getAppId();
        String nxLockRandom = cacheService.getNXLock(nxLockKey, 1L);

        AuthorizerAccessToken weixinAccessToken = null;
        if (null != nxLockRandom) {
            // 重新获取并更新cache
            try {
                weixinAccessToken = getNewAccessTokenFromWeixin(weixinConfig);
                if (StringUtils.isNotBlank(weixinAccessToken.getAuthorizer_access_token())) {
                    setAccessTokenCache(weixinConfig, weixinAccessToken);
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
                    weixinAccessToken = getAccessTokenCache(weixinConfig);
                    break;
                }
            }
        }

        return weixinAccessToken;
    }

    public void setAccessTokenCache(ShopAppDO weixinConfig, AuthorizerAccessToken weixinAccessToken) {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = weixinConfig.getAppId();
        cacheService.hSet(key, field, weixinAccessToken);
        cacheService.expire(key, weixinAccessTokenTimeoutSecounds);
    }

    private AuthorizerAccessToken getNewAccessTokenFromWeixin(ShopAppDO weixinConfig) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("component_appid", componentService.getComponentConfig().getComponentAppId());
        vars.put("authorizer_appid", weixinConfig.getAppId());
        vars.put("authorizer_refresh_token", weixinConfig.getAuthRefreshToken());
        String request = JSONObject.toJSONString(vars);

        vars = new HashMap<>();
        vars.put("component_access_token", componentService.getComponentAccessToken().getComponent_access_token());

        AuthorizerAccessToken weixinAccessToken = httpClientService
                .post(apiAuthorizerToken, request, AuthorizerAccessToken.class, vars);
        if (null == weixinAccessToken) {
            log.error("获取授权appid的令牌无结果|appId={}", weixinConfig.getAppId());
            throw new ThirdPlatformServiceException("获取授权appid的令牌无结果");
        }
        return weixinAccessToken;
    }
}
