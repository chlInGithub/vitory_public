package com.chl.victory.localservice;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/31 10:32
 **/
@Component
public class ShopCouponsLocalService {
    @Resource
    CacheService cacheService;


    public String getCouponsCache(Long shopId, String field) {
        String key = getCouponsCacheKey(shopId);
        String val = cacheService.hGet(key, field, String.class);
        return val;
    }

    private String getCouponsCacheKey(Long shopId) {
        return CacheKeyPrefix.COUPONS_OF_SHOP + shopId;
    }

    public void setCouponsCache(Long shopId, String field, String activityCache) {
        String key = getCouponsCacheKey(shopId);
        cacheService.hSet(key, field, activityCache, CacheExpire.SECONDS_30);
    }

    public void delCouponsCache(Long shopId, String field) {
        String key = getCouponsCacheKey(shopId);
        cacheService.hDel(key, field);
    }



    public void delItemIdsOfCoupons(Long shopId, Long id){
        String key = getCouponItemsCacheKey(shopId, id);
        cacheService.delKey(key);
    }

    public List<Long> getItemIdsOfCoupons(Long shopId, Long id) {
        String key = getCouponItemsCacheKey(shopId, id);
        boolean existsKey = cacheService.existsKey(key);
        if (existsKey) {
            Set<Long> set = cacheService.sScan(key, Long.class);
            if (!CollectionUtils.isEmpty(set)) {
                return set.stream().sorted().collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    public void saveItemIdsOfCoupons(Long shopId, Long id, List<Long> itemIds) {
        String key = getCouponItemsCacheKey(shopId, id);
        if (!CollectionUtils.isEmpty(itemIds)) {
            Set<Long> itemIdSet = itemIds.stream().collect(Collectors.toSet());
            cacheService.sAddAll(key, itemIdSet);
        }
        else {
            cacheService.delKey(key);
        }
    }

    public String getCouponItemsCacheKey(Long shopId, Long id) {
        return CacheKeyPrefix.ITEM_SET_OF_SHOP_AND_COUPONS + shopId + CacheKeyPrefix.SEPARATOR + id;
    }
}
