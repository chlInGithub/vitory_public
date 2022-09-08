package com.chl.victory.localservice;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/31 10:26
 **/
@Component
public class SaleStrategyLocalService {

    @Resource
    CacheService cacheService;

    public void saveStrategyCache(Long shopId, Long id, List<Long> itemIds, Integer dealPoint, String strategyDTOCache) {
        if (!CollectionUtils.isEmpty(itemIds)) {
            saveStrategy(shopId, id, strategyDTOCache);
            saveDealPointStrategy(shopId, id, dealPoint);

            Set<Long> currentItemIds = itemIds.stream().collect(Collectors.toSet());
            Set<Long> oldItemIds = getItemIdsOfStrategy(shopId, id);
            List<Long> delItemIds = oldItemIds.stream().filter(itemId -> !currentItemIds.contains(itemId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(delItemIds)) {
                for (Long delItemId : delItemIds) {
                    delItemStrategy(shopId, id, delItemId);
                    delStrategyItem(shopId, id, delItemId);
                }
            }

            for (Long itemId : itemIds) {
                saveItemStrategy(shopId, id, itemId);
            }
            saveStrategyItems(shopId, id, itemIds);
        }
        else {
            delStrategyCache(shopId, id, dealPoint);
        }
    }

    public void delStrategyCache(@NotNull Long shopId, @NotNull Long strategyId, Integer dealPoint) {
        delStrategy(shopId, strategyId);
        delDealPointStrategy(shopId, dealPoint, strategyId);
        Set<Long> itemIds = getItemIdsOfStrategy(shopId, strategyId);
        for (Long itemId : itemIds) {
            delItemStrategy(shopId, strategyId, itemId);
        }
        delStrategyItems(shopId, strategyId);
    }

    /**
     * 处理位置 对应的  销售策略 集合。
     * @param shopId
     * @param dealPoint
     * @return
     */
    public Set<Long> getStrategysOfDealPoint(Long shopId, Integer dealPoint) {
        String key = getDealPointStrategysCacheKey(shopId, dealPoint);
        boolean existsKey = cacheService.existsKey(key);
        if (existsKey) {
            Set<Long> set = cacheService.sScan(key, Long.class);
            return set;
        }
        return Collections.EMPTY_SET;
    }

    public void saveDealPointStrategy(Long shopId, Long strategyId, Integer dealPoint) {
        String key = getDealPointStrategysCacheKey(shopId, dealPoint);
        cacheService.sAdd(key, strategyId);
    }

    public void delDealPointStrategy(@NotNull Long shopId, Integer dealPoint, @NotNull Long strategyId) {
        if (null == dealPoint) {
            return;
        }
        String key = getDealPointStrategysCacheKey(shopId, dealPoint);
        cacheService.sRem(key, strategyId);
    }

    public void delDealPointStrategy(Long shopId, Integer dealPoint) {
        String key = getDealPointStrategysCacheKey(shopId, dealPoint);
        cacheService.delKey(key);
    }

    /**
     * 向 itemId 对应 策略集合 添加 策略
     * @param shopId
     * @param strategyId
     * @param itemId
     */
    public void saveItemStrategy(Long shopId, Long strategyId, Long itemId) {
        String key = getItemStrategysCacheKey(shopId, itemId);
        cacheService.sAdd(key, strategyId);
    }

    /**
     * itemId 对应 策略集合, 删除某个策略ID
     * @param shopId
     * @param strategyId
     * @param itemId
     */
    public void delItemStrategy(Long shopId, Long strategyId, Long itemId) {
        String key = getItemStrategysCacheKey(shopId, itemId);
        cacheService.sRem(key, strategyId);
    }

    /**
     * itemId 对应 策略集合, 删除key
     * @param shopId
     * @param itemId
     */
    public void delItemStrategy(Long shopId, Long itemId) {
        String key = getItemStrategysCacheKey(shopId, itemId);
        cacheService.delKey(key);
    }

    /**
     * 商品ID 适用的  销售策略 集合。
     * @param shopId
     * @param itemId
     * @return
     */
    public List<Long> getStrategysOfItem(Long shopId, Long itemId) {
        String key = getItemStrategysCacheKey(shopId, itemId);
        boolean existsKey = cacheService.existsKey(key);
        if (existsKey) {
            Set<Long> set = cacheService.sScan(key, Long.class);
            if (!CollectionUtils.isEmpty(set)) {
                return set.stream().collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 策略 对应 itemId集合, 删除某个itemId
     * @param shopId
     * @param strategyId
     */
    public void delStrategyItem(Long shopId, Long strategyId, Long itemId) {
        String key = getStrategyItemsCacheKey(shopId, strategyId);
        cacheService.sRem(key, itemId);
    }

    /**
     * 策略 对应 itemId集合, 删除key
     * @param shopId
     * @param strategyId
     */
    public void delStrategyItems(Long shopId, Long strategyId) {
        String key = getStrategyItemsCacheKey(shopId, strategyId);
        cacheService.delKey(key);
    }

    /**
     * 策略 对应 itemId集合
     * @param shopId
     * @param strategyId
     * @param itemIds
     */
    public void saveStrategyItems(Long shopId, Long strategyId, List<Long> itemIds) {
        String key = getStrategyItemsCacheKey(shopId, strategyId);
        cacheService.sAddAll(key, itemIds.stream().collect(Collectors.toSet()));
    }

    /**
     * 销售策略 适用的  商品ID集合。无数据表示不使用。
     * @param shopId
     * @param strategyId
     * @return
     */
    public Set<Long> getItemIdsOfStrategy(Long shopId, Long strategyId) {
        String key = getStrategyItemsCacheKey(shopId, strategyId);
        boolean existsKey = cacheService.existsKey(key);
        if (existsKey) {
            Set<Long> set = cacheService.sScan(key, Long.class);
            return set;
        }
        return Collections.EMPTY_SET;
    }

    /**
     * 商品ID 是否使用 销售策略
     * @param shopId
     * @param strategyId
     * @param itemId
     * @return
     */
    public boolean isItemOfStrategy(Long shopId, Long strategyId, Long itemId) {
        String key = getStrategyItemsCacheKey(shopId, strategyId);
        boolean sIsMember = cacheService.sIsMember(key, itemId);
        return sIsMember;
    }

    public String getStrategy(@NotNull Long shopId, @NotNull Long strategyId) {
        String key = getStrategyCacheKey(shopId);
        String cache = cacheService.hGet(key, strategyId.toString(), String.class);
        return cache;
    }

    public void delStrategy(@NotNull Long shopId, @NotNull Long strategyId) {
        String key = getStrategyCacheKey(shopId);
        cacheService.hDel(key, strategyId.toString());
    }

    public void saveStrategy(@NotNull Long shopId, @NotNull Long strategyId, @NotEmpty String cache) {
        String key = getStrategyCacheKey(shopId);
        cacheService.hSet(key, strategyId.toString(), cache);
    }

    private String getStrategyItemsCacheKey(Long shopId, Long strategyId) {
        return CacheKeyPrefix.SHOP_STRATEGY_ITEMS_SET + shopId + CacheKeyPrefix.SEPARATOR + strategyId;
    }

    private String getDealPointStrategysCacheKey(Long shopId, Integer dealPoint) {
        return CacheKeyPrefix.SHOP_DEALPOINT_STRATEGYS_SET + shopId + CacheKeyPrefix.SEPARATOR + dealPoint;
    }

    private String getItemStrategysCacheKey(Long shopId, Long itemId) {
        return CacheKeyPrefix.SHOP_ITEM_STRATEGYS_SET + shopId + CacheKeyPrefix.SEPARATOR + itemId;
    }

    private String getStrategyCacheKey(Long shopId) {
        return CacheKeyPrefix.SHOP_STRATEGY_HASH + shopId;
    }
}
