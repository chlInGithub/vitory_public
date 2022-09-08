package com.chl.victory.localservice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/28 16:24
 **/
@Component
public class CartLocalService {
    @Resource
    CacheService cacheService;


    /**
     * 使用hash数据结构缓存购物车数据，key为shopID_userID，field为itemId_skuId,val为count
     */
    public void addCartItem(@NotNull Long shopId, @NotNull Long userId, @NotEmpty String field,
            @Positive int count) {
        String key = CacheKeyPrefix.CART_ITEMS_OF_SHOP_AND_USER + shopId + CacheKeyPrefix.SEPARATOR + userId;
        cacheService.hSet(key, field, count);
    }

    /**
     * 从购物车缓存中删除商品
     */
    public boolean delCartItem(Long shopId, Long userId, String field) {
        String key = CacheKeyPrefix.CART_ITEMS_OF_SHOP_AND_USER + shopId + CacheKeyPrefix.SEPARATOR + userId;
        boolean hDel = cacheService.hDel(key, field);
        return hDel;
    }

    /**
     * 从购物车缓存中获取所有商品
     */
    public Map<String, Integer> getCartItems(Long shopId, Long userId) {
        Map<String, Integer> itemSkuCountMap = _getCartItems(shopId, userId);
        return itemSkuCountMap;
    }

    Map<String, Integer> _getCartItems(Long shopId, Long userId) {
        String key = CacheKeyPrefix.CART_ITEMS_OF_SHOP_AND_USER + shopId + CacheKeyPrefix.SEPARATOR + userId;
        Map<String, Integer> itemSkuCountMap = cacheService.hScan(key, String.class, Integer.class);
        return itemSkuCountMap;
    }

    /**
     * 检查商品是否在购物车中
     * @param items itemId_skuId_count
     * @return
     */
    public boolean checkInCart(Long shopId, Long userId, List<String> items) {
        if (CollectionUtils.isEmpty(items)) {
            return false;
        }

        Map<String, Integer> itemCountMap = _getCartItems(shopId, userId);

        if (CollectionUtils.isEmpty(itemCountMap)) {
            return false;
        }

        Set<String> itemsInCart = itemCountMap.entrySet().stream().map(item -> item.getKey() + "_" + item.getValue())
                .collect(Collectors.toSet());

        for (String item : items) {
            if (!itemsInCart.contains(item)) {
                return false;
            }
        }

        return true;
    }
}
