package com.chl.victory.localservice;

import java.util.ArrayList;
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

import static com.chl.victory.common.constants.ShopConstants.SKU_ID_WHEN_NO_SKU;

/**
 * @author ChenHailong
 * @date 2020/9/1 17:27
 **/
@Component
public class ItemLocalService {
    @Resource
    CacheService cacheService;

    /**
     * 热卖,使用缓存数据，销量作为score，itemId作为val，score前10
     */
    public List<Long> getHotItemIds(@NotNull Long shopId) {
        String key = CacheKeyPrefix.HOTITEM_ITEMIDS_OF_SHOP + shopId;
        long startIndex = -10;
        long endIndex = -1;
        Set<CacheService.ZSetEle<Long>> zSetEles = cacheService.zRange(key, startIndex, endIndex, Long.class);

        List<CacheService.ZSetEle<Long>> zSetEleList = new ArrayList<>(zSetEles);
        Collections.sort(zSetEleList, (o1, o2) -> o1.getScore() > o2.getScore() ? -1 : 1);

        List<Long> idList = new ArrayList<>();
        for (CacheService.ZSetEle<Long> longZSetEle : zSetEleList) {
            idList.add(longZSetEle.getVal());
        }

        return idList;
    }

    public void setTypeItemIds(@NotNull Long shopId, @NotNull Integer type, @NotEmpty List<Long> itemIds) {
        String key = CacheKeyPrefix.TYPE_ITEMIDS_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + type;
        cacheService.delKey(key);

        if (!CollectionUtils.isEmpty(itemIds)) {
            Set<Long> itemIdSet = itemIds.stream().collect(Collectors.toSet());
            cacheService.sAddAll(key, itemIdSet);
        }
        else {
            cacheService.delKey(key);
        }
    }

    public List<Long> getTypeItemIds(@NotNull Long shopId, @NotNull Integer type) {
        String key = CacheKeyPrefix.TYPE_ITEMIDS_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + type;
        Set<Long> set = cacheService.sScan(key, Long.class);
        List<Long> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }


    /**
     * 判断SKUID是否有效
     * @param checkedSkuID
     * @return
     */
    public static boolean isSKU(Long checkedSkuID) {
        if (null != checkedSkuID && !SKU_ID_WHEN_NO_SKU.equals(checkedSkuID)) {
            return true;
        }
        return false;
    }

    /**
     * 售罄商品集合
     * @param shopId
     * @param itemId
     */
    public void addSellOut(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.SELLOUT_OF_SHOP + shopId;
        cacheService.sAdd(key, itemId);
    }

    public void delSellOut(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.SELLOUT_OF_SHOP + shopId;
        cacheService.sRem(key, itemId);
    }

    public Long countSellOut(Long shopId) {
        String key = CacheKeyPrefix.SELLOUT_OF_SHOP + shopId;
        return cacheService.sCard(key);
    }
    public List<Long> getSellOut(Long shopId) {
        String key = CacheKeyPrefix.SELLOUT_OF_SHOP + shopId;
        Set<Long> set = cacheService.sScan(key, Long.class);
        List<Long> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    /**
     * 库存紧张商品
     * @param shopId
     * @param itemId
     */
    public void addLowInventory(Long shopId, String itemId) {
        String key = CacheKeyPrefix.LOW_INVENTORY_OF_SHOP + shopId;
        cacheService.sAdd(key, itemId);
    }
    public void delLowInventory(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.LOW_INVENTORY_OF_SHOP + shopId;
        cacheService.sRem(key, itemId);
    }
    public Long countLowInventory(Long shopId) {
        String key = CacheKeyPrefix.LOW_INVENTORY_OF_SHOP + shopId;
        return cacheService.sCard(key);
    }
    public List<Long> getLowInventory(Long shopId) {
        String key = CacheKeyPrefix.LOW_INVENTORY_OF_SHOP + shopId;
        Set<Long> set = cacheService.sScan(key, Long.class);
        List<Long> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }
}
