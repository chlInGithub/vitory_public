package com.chl.victory.webcommon.service;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.serviceapi.exception.BusServiceException;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/8/31 15:04
 **/
@Component
public class TokenService {
    @Resource
    CacheService cacheService;

    final int maxLastInterval = 30 * 60;

    public String genTokenLast(String sessinId) {
        String last = System.currentTimeMillis() + "";
        String tokenLast = (last + "baseLast").hashCode() + "_" + last;
        cacheService.save("loginToken" + sessinId, tokenLast, maxLastInterval);
        return tokenLast;
    }

    public void addFormToken(@NotEmpty String token, @NotNull String shopIdOrSessionId) {
        String key = CacheKeyPrefix.FORM_TOKEN_SHOP + shopIdOrSessionId;
        cacheService.sAdd(key, token);
        cacheService.expire(key, CacheExpire.HOUR_1);
    }

    public void checkFormToken(@NotEmpty String token, @NotNull String shopIdOrSessionId) throws BusServiceException {
        String key = CacheKeyPrefix.FORM_TOKEN_SHOP + shopIdOrSessionId;
        Long remCount = cacheService.sRem(key, token);
        if (null == remCount || remCount < 1) {
            throw new BusServiceException("表单token缺失");
        }
    }
}
