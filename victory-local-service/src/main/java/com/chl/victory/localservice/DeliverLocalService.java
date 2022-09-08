package com.chl.victory.localservice;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/8/31 10:50
 **/
@Component
public class DeliverLocalService {
    @Resource
    CacheService cacheService;

    public Long getDefaultDeliverId(@NotNull Long shopId, @NotNull Long userId){
        String key = CacheKeyPrefix.DEFAULT_DELIVER_OF_SHOP_AND_USER + shopId;
        Long deliverId = cacheService.hGet(key, userId.toString(), Long.class);
        return deliverId;
    }

    public void setDefaultDeliverId(@NotNull Long shopId, @NotNull Long userId, @NotNull Long deliverId){
        String key = CacheKeyPrefix.DEFAULT_DELIVER_OF_SHOP_AND_USER + shopId;
        String field = userId.toString();
        cacheService.hSet(key, field, deliverId);
    }

    public void delDefaultDeliverId(@NotNull Long shopId, @NotNull Long userId){
        String key = CacheKeyPrefix.DEFAULT_DELIVER_OF_SHOP_AND_USER + shopId;
        String field = userId.toString();
        cacheService.hDel(key, field);
    }
}
