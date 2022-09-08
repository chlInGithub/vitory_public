package com.chl.victory.localservice;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/31 10:26
 **/
@Component
public class ShopActivityLocalService {
    @Resource
    CacheService cacheService;

    public String getActivityCache(Long shopId, String field) {
        String key = getActivityCacheKey(shopId);
        String val = cacheService.hGet(key, field, String.class);
        return val;
    }

    private String getActivityCacheKey(Long shopId) {
        return CacheKeyPrefix.ACTIVITY_OF_SHOP + shopId;
    }

    public void setActivityCache(Long shopId, String field, String activityCache) {
        String key = getActivityCacheKey(shopId);
        cacheService.hSet(key, field, activityCache, CacheExpire.MINUTE_1);
    }

    public void delActivityCache(Long shopId, String field) {
        String key = getActivityCacheKey(shopId);
        cacheService.hDel(key, field);
    }

    public void saveActivityItems(Long shopId, Long activityId, List<Long> itemIds) {
        String key = getActivityItemsCacheKey(shopId, activityId);
        if (!CollectionUtils.isEmpty(itemIds)) {
            Set<Long> itemIdSet = itemIds.stream().collect(Collectors.toSet());
            cacheService.sAddAll(key, itemIdSet);
        }
        else {
            cacheService.delKey(key);
        }
    }


    /**
     * 匹配活动商品
     */
    public List<Long> selectActivityItemIds(@NotNull Long shopId, @NotNull Long actId,
            @NotEmpty List<Long> buyedItemIds) {
        String key = getActivityItemsCacheKey(shopId, actId);
        boolean existKey = cacheService.existsKey(key);
        // key 不存在，表示所有商品
        if (!existKey) {
            return buyedItemIds;
        }

        buyedItemIds = buyedItemIds.stream().filter(item -> cacheService.sIsMember(key, item))
                .collect(Collectors.toList());
        return buyedItemIds;
    }

    public void delActivityItems(Long shopId, Long activityId) {
        String key = getActivityItemsCacheKey(shopId, activityId);
        cacheService.delKey(key);
    }


    /**
     * 参与活动的商品ID集合，空集合表示所有商品。
     * @param shopId
     * @param id
     * @return
     */
    public List<Long> getItemIdsOfActivity(Long shopId, Long id) {
        String key = getActivityItemsCacheKey(shopId, id);
        boolean existsKey = cacheService.existsKey(key);
        if (existsKey) {
            Set<Long> set = cacheService.sScan(key, Long.class);
            if (!CollectionUtils.isEmpty(set)) {
                return set.stream().collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    private String getActivityItemsCacheKey(Long shopId, Long id) {
        return CacheKeyPrefix.ITEM_SET_OF_SHOP_AND_ACTIVITY + shopId + CacheKeyPrefix.SEPARATOR + id;
    }
}
