package com.chl.victory.localservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/28 15:32
 **/
@Component
public class OrderLocalService {
    @Resource
    CacheService cacheService;

    String getKeyOfStatusCountCache(@NotNull Long shopId) {
        String key = CacheKeyPrefix.STATUS_COUNT_OF_ORDER_OF_SHOP_USER + shopId;
        return key;
    }

    public String getStatusCountCache(@NotNull Long shopId, @NotNull Long userId) {
        String key = getKeyOfStatusCountCache(shopId);
        String field = userId.toString();
        String val = cacheService.hGet(key, field, String.class);
        return val;
    }

    public void saveStatusCountCache(@NotNull Long shopId, @NotNull Long userId, @NotNull String val) {
        String key = getKeyOfStatusCountCache(shopId);
        String field = userId.toString();
        cacheService.hSet(key, field, val, CacheExpire.DAYS_1);
    }

    public void delOrderCache2C(@NotNull Long shopId, @NotNull Long userId, Long orderId) {
        delStatusCountCache(shopId, userId);
        delOrderListCache(shopId, userId);
        delOrderDetailCache(shopId, userId, orderId);
        delOrderTotalCache(shopId, userId);
    }

    void delStatusCountCache(Long shopId, Long userId) {
        String key = getKeyOfStatusCountCache(shopId);
        String field = userId.toString();
        cacheService.hDel(key, field);
    }

    String getKeyOfOrderDetailCache(Long shopId, Long userId) {
        String key =
                CacheKeyPrefix.ORDER_DETAIL_OF_ORDER_OF_SHOP_USER_ORDER + shopId + CacheKeyPrefix.SEPARATOR + userId;
        return key;
    }

    public String getOrderDetailCache(@NotNull Long shopId, @NotNull Long userId, @NotNull Long orderId) {
        String key = getKeyOfOrderDetailCache(shopId, userId);
        String field = orderId.toString();
        String val = cacheService.hGet(key, field, String.class);
        return val;
    }

    public void saveOrderDetailCache(@NotNull Long shopId, @NotNull Long userId, @NotNull Long orderId,
            @NotNull String val) {
        String key = getKeyOfOrderDetailCache(shopId, userId);
        String field = orderId.toString();
        cacheService.hSet(key, field, val, CacheExpire.DAYS_1);
    }

    void delOrderDetailCache(Long shopId, Long userId, Long orderId) {
        if (null == orderId) {
            return;
        }
        String key = getKeyOfOrderDetailCache(shopId, userId);
        String field = orderId.toString();
        cacheService.hDel(key, field);
    }

    String getKeyOfOrderListCache(Long shopId, Long userId) {
        String key =
                CacheKeyPrefix.ORDER_LIST_OF_ORDER_OF_SHOP_USER_STATUS + shopId + CacheKeyPrefix.SEPARATOR + userId;
        return key;
    }

    public String getOrderListCache(@NotNull Long shopId, @NotNull Long userId, @NotNull String status) {
        String key = getKeyOfOrderListCache(shopId, userId);
        String field = status;
        String val = cacheService.hGet(key, field, String.class);
        return val;
    }

    public void saveOrderListCache(@NotNull Long shopId, @NotNull Long userId, @NotNull String status,
            @NotNull String val) {
        String key = getKeyOfOrderListCache(shopId, userId);
        String field = status;
        cacheService.hSet(key, field, val, CacheExpire.DAYS_1);
    }

    String getKeyOfOrderTotalCache(Long shopId, Long userId) {
        String key =
                CacheKeyPrefix.ORDER_TOTAL_OF_ORDER_OF_SHOP_USER_STATUS + shopId + CacheKeyPrefix.SEPARATOR + userId;
        return key;
    }

    public Integer getOrderTotalCache(@NotNull Long shopId, @NotNull Long userId, @NotNull String status) {
        String key = getKeyOfOrderTotalCache(shopId, userId);
        String field = status;
        Integer val = cacheService.hGet(key, field, Integer.class);
        return val;
    }

    public void saveOrderTotalCache(@NotNull Long shopId, @NotNull Long userId, @NotNull String status,
            @NotNull Integer val) {
        String key = getKeyOfOrderTotalCache(shopId, userId);
        String field = status;
        cacheService.hSet(key, field, val, CacheExpire.DAYS_1);
    }

    void delOrderTotalCache(Long shopId, Long userId) {
        String key = getKeyOfOrderTotalCache(shopId, userId);
        cacheService.delKey(key);
    }

    void delOrderListCache(Long shopId, Long userId) {
        String key = getKeyOfOrderListCache(shopId, userId);
        cacheService.delKey(key);
    }

    public static List<ItemOfNewOrder> parseItemsOfNewOrder(List<String> itemsFromCreateOrder) throws Exception {
        if (CollectionUtils.isEmpty(itemsFromCreateOrder)) {
            return null;
        }

        List<ItemOfNewOrder> itemsOfNewOrder = new ArrayList<>();

        for (String item : itemsFromCreateOrder) {
            if (StringUtils.isBlank(item)) {
                throw new Exception("商品参数错误:存在empty数据");
            }
            String[] temp = item.split("_");
            Long itemId = NumberUtils.toLong(temp[ 0 ], -1);
            Long skuId = NumberUtils.toLong(temp[ 1 ], -1);
            Integer count = NumberUtils.toInt(temp[ 2 ], -1);
            if (-1 == itemId || -1 == skuId || -1 == count) {
                throw new Exception("缺少商品参数");
            }
            ItemOfNewOrder itemOfNewOrder = new ItemOfNewOrder(itemId, skuId, count);
            itemsOfNewOrder.add(itemOfNewOrder);
        }

        return itemsOfNewOrder;
    }

    @Data
    @AllArgsConstructor
    public static class ItemOfNewOrder {

        Long itemId;

        Long skuId;

        Integer count;
    }

    public void increPresellItemTotal(Long shopId, Long itemId, Integer count) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        String field = itemId.toString();
        cacheService.hIncrement(key, field, count);
    }

    public void decrePresellItemTotal(Long shopId, Long itemId, Integer count) {
        Integer presellItemTotal = getPresellItemTotal(shopId, itemId);
        if (presellItemTotal < 1) {
            return;
        }

        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        String field = itemId.toString();
        cacheService.hIncrement(key, field, -count);
    }

    public Integer getPresellItemTotal(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        String field = itemId.toString();
        Integer total = cacheService.hGet(key, field, Integer.class);
        return total == null || total < 0 ? 0 : total;
    }

    public void delPresell(Long shopId) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        cacheService.delKey(key);
    }
    public void delPresellItemTotal(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        cacheService.hDel(key, itemId.toString());
    }
    public boolean existPresell(Long shopId) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        boolean existsKey = cacheService.existsKey(key);
        return existsKey;
    }

    public Map<Long, Integer> getAllPresellItemTotal(Long shopId) {
        String key = CacheKeyPrefix.SHOP_PRESELL_ITEM_TOTAL_HASH + shopId;
        Map<Long, Integer> itemIdMapTotal = cacheService.hScan(key, Long.class, Integer.class);
        return itemIdMapTotal;
    }
}
