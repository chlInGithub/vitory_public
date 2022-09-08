package com.chl.victory.webcommon.service;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.webcommon.model.SessionCache;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 自定义session 服务
 * @author ChenHailong
 * @date 2019/4/23 14:07
 **/
@Service
@Validated
public class SessionService {

    static int expireSeconds = 3600;

    /**
     * 距超时还有x秒，对session cache进行更新
     */
    static int intervalSeconds = 60 * 10;

    final String sessionCacheKey = "sessionCache:";

    @Resource
    CacheService cacheService;

    /**
     * 从cache中获取
     * @param sessionCacheId
     * @return
     */
    public <T extends SessionCache> T getSession(@NotBlank String keySuffix, @NotBlank String sessionCacheId, Class<T> sessionCacheClazz) {
        String sessionCacheKey = getSessionCacheKey(keySuffix, sessionCacheId);
        return cacheService.get(sessionCacheKey, sessionCacheClazz);
    }

    /**
     * 保存到cache
     * @param sessionCacheId
     * @param sessionCache
     * @return
     */
    public void setSession(@NotBlank String keySuffix, @NotBlank String sessionCacheId, @NotNull SessionCache sessionCache) {
        sessionCache.setModifiedTime(System.currentTimeMillis());
        sessionCache.setKey(sessionCacheId);
        String sessionCacheKey = getSessionCacheKey(keySuffix, sessionCacheId);
        cacheService.save(sessionCacheKey, sessionCache, SessionService.expireSeconds);
    }

    public void refresh(@NotBlank String keySuffix,@NotBlank String sessionCacheId, @NotNull SessionCache sessionCache) {
        if (sessionCache.getModifiedTime() != null) {
            Long curentSecond = System.currentTimeMillis() / 1000;
            Long lastSecond = sessionCache.getModifiedTime() / 1000;
            if (curentSecond - lastSecond > (SessionService.expireSeconds - SessionService.intervalSeconds)) {
                sessionCache.setModifiedTime(System.currentTimeMillis());
                setSession(keySuffix, sessionCacheId, sessionCache);
            }
        }
    }

    String getSessionCacheKey(String keySuffix, String sessionCacheId){
        return sessionCacheKey + keySuffix + CacheKeyPrefix.SEPARATOR + sessionCacheId;
    }

    public void delSession(String keySuffix, Long sessionCacheId) {
        cacheService.delKey(getSessionCacheKey(keySuffix, sessionCacheId.toString()));
    }
}
