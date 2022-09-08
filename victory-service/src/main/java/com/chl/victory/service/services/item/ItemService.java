package com.chl.victory.service.services.item;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.item.ItemManager;
import com.chl.victory.dao.manager.order.OrderManager;
import com.chl.victory.dao.model.item.CategoryDO;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.model.item.ItemLogisticsConfigDO;
import com.chl.victory.dao.model.item.SkuDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.BaseQuery;
import com.chl.victory.dao.query.item.CategoryQuery;
import com.chl.victory.dao.query.item.ItemQuery;
import com.chl.victory.dao.query.item.SkuQuery;
import com.chl.victory.dao.query.order.SubOrderQuery;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.serviceapi.item.enums.ItemStatusEnum;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.ItemSummaryDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.CategoryQueryDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.common.constants.ShopConstants.SKU_ID_WHEN_NO_SKU;
import static com.chl.victory.localservice.manager.LocalServiceManager.saleStrategyLocalService;
import static com.chl.victory.service.services.ServiceManager.cacheService;

/**
 * @author ChenHailong
 * @date 2019/5/7 16:28
 **/
@Service
@Validated
@Slf4j
public class ItemService extends BaseService {

    @Resource
    ItemManager itemManager;

    @Resource
    OrderManager orderManager;

    public static ItemLogisticsConfigDO getLogisticsConfig(String itemLogistics) {
        if (StringUtils.isEmpty(itemLogistics)) {
            return null;
        }
        ItemLogisticsConfigDO itemLogisticsConfigDO = JSONObject
                .parseObject(itemLogistics, ItemLogisticsConfigDO.class);
        return itemLogisticsConfigDO;
    }

    public static Long castItemId(String itemId) throws BusServiceException {
        if (NumberUtils.isDigits(itemId)) {
            return Long.valueOf(itemId);
        }
        throw new BusServiceException("itemId格式错误");
    }

    public static Long castSkuId(String skuId) throws BusServiceException {
        if (NumberUtils.isDigits(skuId)) {
            return Long.valueOf(skuId);
        }
        throw new BusServiceException("skuId格式错误");
    }

    public ServiceResult saveCate(@NotNull CategoryDTO dto) {
        CategoryDO model = toDO(dto, CategoryDO.class);
        ValidationUtil.validate(model);
        try {
            return ServiceResult.success(itemManager.saveCategory(model));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 删除类目，无法删除存在子类目的类目
     * @param shopId
     * @param cateId
     * @return
     */
    public ServiceResult delCate(@NotNull Long shopId, @NotNull Long cateId) {
        try {
            CategoryQuery selectQuery = new CategoryQuery();
            selectQuery.setShopId(shopId);
            selectQuery.setParentId(cateId);
            if (itemManager.countCate(selectQuery) > 0) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "存在子类目，无法删除");
            }

            selectQuery.setId(cateId);
            selectQuery.setParentId(null);
            return ServiceResult.success(itemManager.delCate(selectQuery));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<CategoryDTO>> selectCates(@NotNull CategoryQueryDTO queryDTO) {
        CategoryQuery query = toQuery(queryDTO, CategoryQuery.class);
        ValidationUtil.validate(query);
        try {
            List<CategoryDO> categoryDOS = itemManager.selectCates(query);
            List<CategoryDTO> categoryDTOS = toDTOs(categoryDOS, CategoryDTO.class);
            return ServiceResult.success(categoryDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    List<CategoryDO> selectCates(@NotNull CategoryQuery query) throws DaoManagerException {
        ValidationUtil.validate(query);
        List<CategoryDO> categoryDOS = itemManager.selectCates(query);
        return categoryDOS;
    }

    public ServiceResult<Integer> countCate(@NotNull CategoryQueryDTO queryDTO) {
        CategoryQuery query = toQuery(queryDTO, CategoryQuery.class);
        ValidationUtil.validate(query);
        try {
            return ServiceResult.success(itemManager.countCate(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 保存商品和sku
     * @return
     */
    public ServiceResult save(@NotNull ItemDO itemDO, List<SkuDO> skuDOS) {
        ValidationUtil.validate(itemDO);
        if (!CollectionUtils.isEmpty(skuDOS)) {
            skuDOS.forEach(ValidationUtil::validate);
        }

        // 数据修正
        fix(itemDO, skuDOS);

        CategoryQuery categoryQuery = new CategoryQuery();
        categoryQuery.setId(itemDO.getLeafCateId());
        categoryQuery.setShopId(itemDO.getShopId());
        List<CategoryDO> categoryDOS = null;
        try {
            categoryDOS = selectCates(categoryQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
        if (CollectionUtils.isEmpty(categoryDOS) || categoryDOS.size() > 1 || categoryDOS.get(0).getLevel() != 3) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "类目错误，必须叶子类目");
        }

        boolean execute = transactionTemplate.execute(status -> {
            try {
                // save item
                boolean isInsert = itemDO.getId() == null;
                if (isInsert) {
                    itemDO.setStatus(ItemStatusEnum.published.getCode().byteValue());
                }
                itemManager.saveItem(itemDO);
                if (isInsert && itemDO.getId() == null) {
                    log.error("新建商品却无ID");
                    status.setRollbackOnly();
                    return false;
                }
                if (!isInsert) {
                    delSkus(itemDO.getId(), skuDOS);
                }

                if (!CollectionUtils.isEmpty(skuDOS)) {
                    for (SkuDO skuDO : skuDOS) {
                        skuDO.setItemId(itemDO.getId());
                        itemManager.saveSku(skuDO);
                    }
                }

                return true;
            } catch (Exception e) {
                log.error("saveItem ex", e);
                status.setRollbackOnly();
                return false;
            }
        });

        if (execute) {
            if (itemDO.getInventory() > 0) {
                LocalServiceManager.itemLocalService.delSellOut(itemDO.getShopId(), itemDO.getId());
                LocalServiceManager.itemLocalService.delLowInventory(itemDO.getShopId(), itemDO.getId());
            }
            // 商品是否有sku
            String key = CacheKeyPrefix.ITEM_SET_THAT_HAS_SKU_OF_SHOP + itemDO.getShopId();
            if (!CollectionUtils.isEmpty(skuDOS)) {
                cacheService.sAdd(key, itemDO.getId().toString());
            }
            else {
                cacheService.sRem(key, itemDO.getId().toString());
            }

            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX);
    }
    public ServiceResult save(@NotNull ItemDTO itemDTO, List<SkuDTO> skuDTOS) {
        ItemDO itemDO = toDO(itemDTO, ItemDO.class);
        List<SkuDO> skuDOS = toDOs(skuDTOS, SkuDO.class);
        return save(itemDO, skuDOS);
    }

    private void savePresellItemSet(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.PRESELL_ITEM_SET_OF_SHOP + shopId;
        cacheService.sAdd(key, itemId);
    }

    private void removePresellItemSet(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.PRESELL_ITEM_SET_OF_SHOP + shopId;
        cacheService.sRem(key, itemId);
    }

    public boolean existPresellItemSet(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.PRESELL_ITEM_SET_OF_SHOP + shopId;
        return cacheService.sIsMember(key, itemId);
    }

    private void fix(ItemDO itemDO, List<SkuDO> skuDOS) {
        fillDefaultData(itemDO);
        fillDefualtData(skuDOS);
        if (!CollectionUtils.isEmpty(skuDOS)) {
            itemDO.setInventory((int) skuDOS.stream().collect(Collectors.summarizingInt(SkuDO::getInventory)).getSum());
        }
    }

    private void fillDefualtData(List<SkuDO> skuDOS) {
        for (SkuDO skuDO : skuDOS) {
            if (skuDO.getSales() == null) {
                skuDO.setSales(0);
            }
        }
    }

    private void fillDefaultData(ItemDO itemDO) {
        if (itemDO.getSales() == null) {
            itemDO.setSales(0);
        }
    }

    public ServiceResult<List<ItemDTO>> selectItems(@NotNull ItemQueryDTO queryDTO) {
        ItemQuery query = toQuery(queryDTO, ItemQuery.class);
        ValidationUtil.validate(query);
        try {
            List<ItemDO> itemDOS = itemManager.selectItems(query);
            if (query.isNeedFillCacheData()) {
                if (!CollectionUtils.isEmpty(itemDOS)) {
                    itemDOS.forEach(item -> {
                        fillSaleUV(item.getShopId(), item);
                        fillSales(item.getShopId(), item);
                        item.setExistSku(existSku(item.getId(), item.getShopId()));
                        fillSaleStrategy(item);
                    });
                }
            }
            List<ItemDTO> itemDTOS = toDTOs(itemDOS, ItemDTO.class);
            return ServiceResult.success(itemDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    private void fillSaleStrategy(ItemDO item) {
        List<Long> strategysOfItem = saleStrategyLocalService.getStrategysOfItem(item.getShopId(), item.getId());
        if (!CollectionUtils.isEmpty(strategysOfItem)) {
            List<String> strategies = strategysOfItem.stream().map(i ->{
                String strategy = saleStrategyLocalService.getStrategy(item.getShopId(), i);
                return strategy;
            }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(strategies)) {
                item.setSaleStrategies(strategies);
            }
        }
    }

    public ServiceResult<List<SkuDTO>> selectSkus(@NotNull SkuQueryDTO queryDTO) {
        SkuQuery query = toQuery(queryDTO, SkuQuery.class);
        ValidationUtil.validate(query);
        try {
            List<SkuDO> skuDOS = itemManager.selectSkus(query);
            List<SkuDTO> skuDTOS = toDTOs(skuDOS, SkuDTO.class);
            return ServiceResult.success(skuDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<Integer> countItem(@NotNull ItemQueryDTO queryDTO) {
        ItemQuery query = toQuery(queryDTO, ItemQuery.class);
        ValidationUtil.validate(query);
        try {
            return ServiceResult.success(itemManager.countItem(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public void assertExistItem(@NotNull ItemQuery query) throws BusServiceException {
        ValidationUtil.validate(query);
        try {
            int countItem = itemManager.countItem(query);
            if (countItem < 1) {
                throw new NotExistException("商品不存在");
            }
        } catch (DaoManagerException e) {
            throw new BusServiceException(trimExMsg(e));
        }
    }

    /**
     * 删除商品，同时删除对应sku
     * @param shopId
     * @param itemIds
     * @return
     */
    public ServiceResult delItem(@NotNull Long shopId, @NotEmpty List<Long> itemIds) {
        try {
            for (Long itemId : itemIds) {
                boolean exist = countOfSomeStatus(itemId, ItemStatusEnum.published.getCode(), shopId);
                if (!exist) {
                    continue;
                }

                ItemQuery query = new ItemQuery();
                query.setId(itemId);
                query.setShopId(shopId);
                int affectRow = itemManager.delItem(query);
                if (1 == affectRow) {
                    SkuQuery skuQuery = new SkuQuery();
                    skuQuery.setItemId(itemId);
                    itemManager.delSku(skuQuery);

                    delItemCache(shopId, itemId);
                }
            }
            return ServiceResult.success();
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 删除商品维度cache
     * @param shopId
     * @param itemId
     */
    private void delItemCache(Long shopId, Long itemId) {
        removePresellItemSet(shopId, itemId);
        delSaleCache(shopId, itemId);
        delInventoryCache(shopId, itemId);
    }

    /**
     * 商品当前sku集合 - 保留的sku集合 = 需删除的sku集合
     * @param itemId
     * @param skuDOS 保留的sku
     * @throws DaoManagerException
     */
    private void delSkus(@NotNull Long itemId, @NotEmpty List<SkuDO> skuDOS) throws DaoManagerException {
        // delete sku
        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        List<SkuDO> preSkus = itemManager.selectSkus(skuQuery);
        if (!CollectionUtils.isEmpty(preSkus)) {
            final Set<Long> nowSkuIds;
            if (!CollectionUtils.isEmpty(skuDOS)) {
                nowSkuIds = skuDOS.stream().map(sku -> sku.getId()).filter(e -> null != e).collect(Collectors.toSet());
            }
            else {
                nowSkuIds = null;
            }

            Set<Long> needDelSkuIds;
            if (CollectionUtils.isEmpty(nowSkuIds)) {
                needDelSkuIds = preSkus.stream().map(sku -> sku.getId()).filter(e -> null != e)
                        .collect(Collectors.toSet());
            }
            else {
                needDelSkuIds = preSkus.stream().filter(sku -> !nowSkuIds.contains(sku.getId())).map(sku -> sku.getId())
                        .collect(Collectors.toSet());
            }

            if (!CollectionUtils.isEmpty(needDelSkuIds)) {
                for (Long needDelSkuId : needDelSkuIds) {
                    SkuQuery query = new SkuQuery();
                    query.setId(needDelSkuId);
                    itemManager.delSku(query);
                }
            }
        }
    }

    /**
     * 商品状态是否大于等于某个状态
     */
    public boolean countOfLeStatus(@NotNull @Positive Long itemId, @NotNull @Positive Integer status,
            @NotNull @Positive Long shopId) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setShopId(shopId);
        itemQuery.setId(itemId);
        itemQuery.setStatusStart(status.byteValue());
        int countItem = 0;
        try {
            countItem = itemManager.countItem(itemQuery);
        } catch (DaoManagerException e) {
        }
        return countItem == 0;
    }

    /**
     * 商品状态是否等于某个状态
     */
    public boolean countOfSomeStatus(@NotNull @Positive Long itemId, @NotNull @Positive Integer status,
            @NotNull @Positive Long shopId) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setId(itemId);
        itemQuery.setStatus(status.byteValue());
        int countItem = 0;
        try {
            countItem = itemManager.countItem(itemQuery);
        } catch (DaoManagerException e) {
        }
        return countItem > 0;
    }

    public ServiceResult updateStatus(@NotNull @Positive Long itemId, @NotNull @Positive Integer status,
            @NotNull @Positive Long shopId) {
        try {
            // 状态关系检查 已上线的几个状态 必须 先下线再上线，不能直接转换为准备上架
            if (status.equals(ItemStatusEnum.willShelf.getCode())) {
                boolean exist = countOfLeStatus(itemId, ItemStatusEnum.sale.getCode(), shopId);
                if (!exist) {
                    return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK);
                }
            }

            ItemDO itemDO = new ItemDO();
            itemDO.setId(itemId);
            itemDO.setShopId(shopId);
            itemDO.setStatus(status.byteValue());
            itemManager.saveItem(itemDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        return ServiceResult.success();
    }

    public void fillSaleUV(Long shopId, ItemDO itemDO) {
        // 从cache获取商品对应的购买用户量
        String key = CacheKeyPrefix.USER_COUNT_OF_SHOP + shopId;
        Integer userCount = cacheService.hGet(key, itemDO.getId().toString(), Integer.class);
        itemDO.setSaleUV(userCount);
    }

    public void fillSales(Long shopId, ItemDO itemDO) {
        // 从cache获取商品对应的购买用户量
        String key = CacheKeyPrefix.SALE_OF_SHOP + shopId;
        Integer sales = cacheService.hGet(key, itemDO.getId().toString(), Integer.class);
        itemDO.setSales(sales);
    }

    public void fillSaleUsers(Long shopId, ItemDO itemDO) {
        // 购买商品的用户头像
        String key = CacheKeyPrefix.LAST_BUYERIMG_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + itemDO.getId();
        List<String> val = cacheService.lRange(key, String.class);
        itemDO.setSaleUsers(val);
    }

    public boolean addHotItemId(@NotNull Long shopId, @NotNull String itemId, @NotNull Integer sales) {
        String key = CacheKeyPrefix.HOTITEM_ITEMIDS_OF_SHOP + shopId;
        boolean zAdd = cacheService.zAdd(key, new CacheService.ZSetEle<>(itemId, sales));
        return zAdd;
    }

    public boolean delHotItemId(@NotNull Long shopId, @NotNull Long itemId) {
        String key = CacheKeyPrefix.HOTITEM_ITEMIDS_OF_SHOP + shopId;
        Long zRem = cacheService.zRem(key, itemId);
        return zRem > 0;
    }

    ItemDO assertItemExist(Long shopId, Long itemId) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setId(itemId);
        itemQuery.setShopId(shopId);
        ItemDO itemDO = null;
        try {
            itemDO = itemManager.selectItem(itemQuery);
        } catch (DaoManagerException e) {
            throw new NotExistException("商品不存在", e);
        }

        if (null == itemDO) {
            throw new NotExistException("商品不存在");
        }

        return itemDO;
    }

    /**
     * 上架
     * @param shopId
     * @param itemId
     * @return
     */
    public ServiceResult shelf(Long shopId, Long itemId) {
        ItemDO itemDO = assertItemExist(shopId, itemId);

        // 更新状态
        ServiceResult statusResult = updateStatus(itemId, ItemStatusEnum.willShelf.getCode(), shopId);
        if (!statusResult.getSuccess()) {
            return statusResult;
        }

        // 向cache中添加商品信息与可用库存
        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        skuQuery.setShopId(shopId);
        List<SkuDO> skuDOS = null;
        try {
            skuDOS = itemManager.selectSkus(skuQuery);
        } catch (DaoManagerException e) {
            log.warn("shelf selectSkus ex", e);
        }

        // 设置cache：库存 销量
        if (!CollectionUtils.isEmpty(skuDOS)) {
            Map<String, Integer> itemAndInventoryFromSku = getItemAndInventoryFromSku(skuDOS);
            setInventoryCache(shopId, itemAndInventoryFromSku);
        }

        Map<String, Integer> itemAndSales = new HashMap<>();
        itemAndSales.put(itemId.toString(), itemDO.getSales());
        setSaleCache(shopId, itemAndSales);

        // 设置为出售中
        updateStatus(itemId, ItemStatusEnum.sale.getCode(), shopId);

        return ServiceResult.success();
    }

    /**
     * 下架
     * @param shopId
     * @param itemId
     * @return
     */
    public ServiceResult soldOut(Long shopId, Long itemId) {
        // ItemDO itemDO = assertItemExist(shopId, itemId);

        // 更新状态
        ServiceResult statusResult = updateStatus(itemId, ItemStatusEnum.published.getCode(), shopId);
        if (!statusResult.getSuccess()) {
            return statusResult;
        }

        // 向cache中添加商品信息与可用库存
        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        skuQuery.setShopId(shopId);
        List<SkuDO> skuDOS = null;
        try {
            skuDOS = itemManager.selectSkus(skuQuery);
        } catch (DaoManagerException e) {
            log.warn("selectSkus ex", e);
        }

        if (!CollectionUtils.isEmpty(skuDOS)) {
            Set<String> itemSet = getItemSet(skuDOS);
            delInventoryCache(shopId, itemSet);
        }

        Set<String> itemSet = new HashSet<>();
        itemSet.add(itemId.toString());
        delSaleCache(shopId, itemSet);

        return ServiceResult.success();
    }

    public String getItemSummary(@NotNull Long shopId) {
        String key = CacheKeyPrefix.DASHBOARD_ITEM_SUMMARY_OF_SHOP;
        String cache = cacheService.hGet(key, shopId.toString(), String.class);

        if (null == cache) {
            try {
                ItemQuery itemQuery = new ItemQuery();
                itemQuery.setShopId(shopId);
                int total = itemManager.countItem(itemQuery);

                itemQuery.setStatus(ItemStatusEnum.sale.getCode().byteValue());
                int onSaleTotal = itemManager.countItem(itemQuery);

                SubOrderQuery orderQuery = new SubOrderQuery();
                orderQuery.setShopId(shopId);
                Date firstDayOfCurrentMonth = DateUtils.truncate(new Date(), Calendar.MONTH);
                orderQuery.setStartedCreatedTime(firstDayOfCurrentMonth);
                Integer currentMonthSoldTotal = orderManager.countItem(orderQuery);

                ItemSummaryDTO itemSummaryDTO = new ItemSummaryDTO();
                itemSummaryDTO.setTotal(total);
                itemSummaryDTO.setOnSaleTotal(onSaleTotal);
                itemSummaryDTO.setCurrentMonthSoldTotal(currentMonthSoldTotal);
                cache = JSONObject.toJSONString(itemSummaryDTO);
            } catch (Exception e) {
                log.error("getItemSummary{}", shopId, e);
            }

            if (null != cache) {
                cacheService.hSet(key, shopId, cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }

        return cache;
    }

    /**
     * 使用该方法必须外层包装事务，update是当前读，所以不需要考虑多线程操作数据问题
     * <br/><b>requested shopId</b>
     * @param subOrderDOS
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deductInventory(List<SkuDO> subOrderDOS) throws DaoManagerException {
        for (SkuDO skuDO : subOrderDOS) {
            itemManager.deductInventory(skuDO);
        }
    }

    /**
     * 检查库存
     * @param subOrderDOS
     * @return
     * @throws DaoManagerException
     */
    public boolean verifyDeductInventory(List<SkuDO> subOrderDOS) {
        for (SkuDO skuDO : subOrderDOS) {
            boolean ok = itemManager.verifyDeductInventory(skuDO);
            if (!ok) {
                // 售罄商品
                LocalServiceManager.itemLocalService.addSellOut(skuDO.getShopId(), skuDO.getItemId());
                return false;
            }
        }
        return true;
    }

    public void verifyDeductInventory(OrderQueryDTO query) throws BusServiceException {
        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setOrderId(query.getId());
        subOrderQuery.setShopId(query.getShopId());
        List<SubOrderDO> subOrderDOS;
        try {
            subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        } catch (DaoManagerException e) {
            log.error("verifyDeductInventory{}", query.getId(), e);
            throw new BusServiceException("验证库存时，查询子订单异常.");
        }

        List<SkuDO> skuDOs = getSkuDOs(subOrderDOS);

        for (SkuDO skuDO : skuDOs) {
            boolean ok = itemManager.verifyDeductInventory(skuDO);
            if (!ok) {
                throw new BusServiceException("SKU库存不足");
            }
        }
    }

    public List<SkuDO> getSkuDOs(@NotEmpty List<SubOrderDO> subOrderDOS) {
        List<SkuDO> skuDOS = subOrderDOS.stream().map(subOrderDO -> {
            SkuDO skuDO = new SkuDO();
            skuDO.setShopId(subOrderDO.getShopId());
            skuDO.setId(subOrderDO.getSkuId());
            skuDO.setItemId(subOrderDO.getItemId());
            skuDO.setSales(subOrderDO.getCount().intValue());
            skuDO.setInventory(subOrderDO.getCount().intValue());
            return skuDO;
        }).collect(Collectors.toList());

        return skuDOS;
    }

    /**
     * DB操作
     * <br/><b>requested shopId</b>
     * @param subOrderDOS
     * @return
     * @throws DaoManagerException
     */
    @Transactional(rollbackFor = Throwable.class)
    public void addInventory(List<SkuDO> subOrderDOS) throws DaoManagerException {
        for (SkuDO skuDO : subOrderDOS) {
            itemManager.addInventory(skuDO);
        }
    }

    /**
     * DB操作
     * <br/><b>requested shopId</b>
     * @param skuDOS
     * @return
     * @throws DaoManagerException
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean addSales(List<SkuDO> skuDOS) throws DaoManagerException {
        for (SkuDO skuDO : skuDOS) {
            itemManager.addSales(skuDO);
        }
        return false;
    }

    public Map<String, Integer> getItemAndInventoryFromSku(List<SkuDO> list) {
        Map<String, Integer> fieldAndVal = new HashMap<>();
        for (SkuDO model : list) {
            String field = model.getItemId().toString();
            Integer val = fieldAndVal.get(field);
            if (null == val) {
                val = 0;
            }
            val += model.getInventory();
            fieldAndVal.put(field, val);

            if (model.getId() > 0) {
                field = model.getItemId() + CacheKeyPrefix.SEPARATOR + model.getId();
                val = fieldAndVal.get(field);
                if (null == val) {
                    val = 0;
                }
                val += model.getInventory();
                fieldAndVal.put(field, val);
            }
        }
        return fieldAndVal;
    }

    public Set<String> getItemSet(List<SkuDO> list) {
        Set<String> sets = new HashSet<>();
        for (SkuDO model : list) {
            String field = model.getItemId().toString();
            sets.add(field);

            if (model.getId() > 0) {
                field = model.getItemId() + CacheKeyPrefix.SEPARATOR + model.getId();
                sets.add(field);
            }
        }
        return sets;
    }

    public Map<String, Integer> getItemAndInventoryFromSubOrder(List<SubOrderDO> subOrderDOS) {
        Map<String, Integer> fieldAndVal = new HashMap<>();
        for (SubOrderDO model : subOrderDOS) {
            String field = model.getItemId().toString();
            Integer val = fieldAndVal.get(field);
            if (null == val) {
                val = 0;
            }
            val += model.getCount();
            fieldAndVal.put(field, val);

            if (model.getSkuId() > 0) {
                field = model.getItemId() + CacheKeyPrefix.SEPARATOR + model.getSkuId();
                val = fieldAndVal.get(field);
                if (null == val) {
                    val = 0;
                }
                val += model.getCount();
                fieldAndVal.put(field, val);
            }
        }
        return fieldAndVal;
    }

    public Set<String> getItemSet(List<SubOrderDO> subOrderDOS, boolean needSku) {
        Set<String> itemSet = new HashSet<>();
        for (SubOrderDO model : subOrderDOS) {
            String field = model.getItemId().toString();
            itemSet.add(field);

            if (needSku && model.getSkuId() > 0) {
                field = model.getItemId() + CacheKeyPrefix.SEPARATOR + model.getSkuId();
                itemSet.add(field);
            }
        }
        return itemSet;
    }

    /**
     * 设置库存cache
     * @param shopId
     * @param itemAndInventory {@link ItemService#getItemAndInventoryFromSku(List)}
     */
    public void setInventoryCache(Long shopId, Map<String, Integer> itemAndInventory) {
        if (null == shopId || CollectionUtils.isEmpty(itemAndInventory)) {
            return;
        }

        String key = CacheKeyPrefix.INVENTORY_OF_SHOP + shopId;
        cacheService.hSet(key, itemAndInventory);
    }

    public void delInventoryCache(Long shopId, Set<String> itemSet) {
        if (null == shopId || CollectionUtils.isEmpty(itemSet)) {
            return;
        }

        String key = CacheKeyPrefix.INVENTORY_OF_SHOP + shopId;
        for (String field : itemSet) {
            cacheService.hDel(key, field);
        }
    }

    public void delInventoryCache(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.INVENTORY_OF_SHOP + shopId;
        cacheService.hDel(key, itemId);
    }

    /**
     * 增加库存cache
     * @param shopId
     * @param itemAndInventory {@link ItemService#getItemAndInventoryFromSubOrder(List)}
     */
    public void addInventoryCache(Long shopId, Map<String, Integer> itemAndInventory) {
        if (null == shopId || CollectionUtils.isEmpty(itemAndInventory)) {
            return;
        }

        String key = CacheKeyPrefix.INVENTORY_OF_SHOP + shopId;
        for (Map.Entry<String, Integer> entry : itemAndInventory.entrySet()) {
            cacheService.hIncrement(key, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 减少库存cache
     * @param shopId
     * @param itemAndInventory {@link ItemService#getItemAndInventoryFromSubOrder(List)}
     */
    public void deductInventoryCache(Long shopId, Map<String, Integer> itemAndInventory) {
        if (null == shopId || CollectionUtils.isEmpty(itemAndInventory)) {
            return;
        }

        String key = CacheKeyPrefix.INVENTORY_OF_SHOP + shopId;
        for (Map.Entry<String, Integer> entry : itemAndInventory.entrySet()) {
            Long current = cacheService.hIncrement(key, entry.getKey(), -entry.getValue());

            if (current <= ShopConstants.INVENTORY_MIN_LIMIT) {
                LocalServiceManager.itemLocalService.addLowInventory(shopId, entry.getKey());
            }
        }
    }

    /**
     * 设置商品销量cache
     * @param shopId
     * @param itemAndCount
     */
    public void setSaleCache(Long shopId, Map<String, Integer> itemAndCount) {
        if (null == shopId || CollectionUtils.isEmpty(itemAndCount)) {
            return;
        }

        String key = CacheKeyPrefix.SALE_OF_SHOP + shopId;
        for (Map.Entry<String, Integer> entry : itemAndCount.entrySet()) {
            cacheService.hSet(key, entry.getKey(), entry.getValue());
            addHotItemId(shopId, entry.getKey(), entry.getValue());
        }
    }

    public void delSaleCache(Long shopId, Set<String> itemSet) {
        if (null == shopId || CollectionUtils.isEmpty(itemSet)) {
            return;
        }

        String key = CacheKeyPrefix.SALE_OF_SHOP + shopId;
        for (String field : itemSet) {
            cacheService.hDel(key, field);
        }
    }

    void delSaleCache(Long shopId, Long itemId) {
        String key = CacheKeyPrefix.SALE_OF_SHOP + shopId;
        cacheService.hDel(key, itemId);
        delHotItemId(shopId, itemId);
    }

    /**
     * 增加商品销量cache
     * @param shopId
     * @param itemAndCount {@link ItemService#getItemAndInventoryFromSubOrder(List)}
     */
    public void addSaleCache(Long shopId, Map<String, Integer> itemAndCount) {
        if (null == shopId || CollectionUtils.isEmpty(itemAndCount)) {
            return;
        }

        String key = CacheKeyPrefix.SALE_OF_SHOP + shopId;
        for (Map.Entry<String, Integer> entry : itemAndCount.entrySet()) {
            String[] ids = entry.getKey().split("_");
            Long sales = cacheService.hIncrement(key, entry.getKey(), entry.getValue());
            if (ids.length > 1){
                sales = cacheService.hIncrement(key, ids[0], entry.getValue());
                addHotItemId(shopId, ids[0], sales.intValue());
            }else {
                addHotItemId(shopId, entry.getKey(), sales.intValue());
            }
        }
    }

    /**
     * 增加商品买家数量cache
     * @param shopId
     * @param itemSet {@link ItemService#getItemSet(List, boolean)}
     */
    public void addUserCountCache(Long shopId, Set<String> itemSet) {
        if (null == shopId || CollectionUtils.isEmpty(itemSet)) {
            return;
        }

        String key = CacheKeyPrefix.USER_COUNT_OF_SHOP + shopId;
        for (String item : itemSet) {
            cacheService.hIncrement(key, item, 1);
        }
    }

    /**
     * 增加商品买家图片cache
     * @param shopId
     * @param itemSet {@link ItemService#getItemSet(List, boolean)}
     */
    public void addUserImgsCache(Long shopId, String buyerImgUrl, Set<String> itemSet) {
        if (null == shopId || StringUtils.isEmpty(buyerImgUrl) || CollectionUtils.isEmpty(itemSet)) {
            return;
        }

        String keyPre = CacheKeyPrefix.LAST_BUYERIMG_OF_SHOP + shopId;
        for (String item : itemSet) {
            String key = keyPre + CacheKeyPrefix.SEPARATOR + item;
            cacheService.lPushAndTrim(key, buyerImgUrl, 20, CacheExpire.DAYS_30);
        }
    }

    public List<Long> selectAllCachedLeafCateIds(Long cateId, Long shopId) {
        String key = CacheKeyPrefix.CATE_OF_SHOP + shopId;
        String cateIdStr = cacheService.hGet(key, cateId.toString(), String.class);

        List<Long> cateIds = new ArrayList<>();
        if (!StringUtils.isEmpty(cateIdStr)) {
            String[] arrays = cateIdStr.split(",");
            for (String temp : arrays) {
                Long t = Long.valueOf(temp);
                cateIds.add(t);
            }
        }

        if (CollectionUtils.isEmpty(cateIds)) {
            cateIds.addAll(selectAllLeafCateIds(cateId, shopId));
        }

        if (!CollectionUtils.isEmpty(cateIds)) {
            cacheService
                    .hSet(key, cateId, StringUtils.collectionToDelimitedString(cateIds, ","), CacheExpire.MINUTE_10);
        }

        return cateIds;
    }

    public List<Long> selectAllLeafCateIds(Long cateId, Long shopId) {
        List<Long> cateIds = new ArrayList<>();
        CategoryQuery categoryQuery = new CategoryQuery();
        categoryQuery.setParentId(cateId);
        categoryQuery.setShopId(shopId);
        int countCate = 0;
        try {
            countCate = itemManager.countCate(categoryQuery);
        } catch (DaoManagerException e) {
        }
        if (countCate < 1) {
            cateIds.add(cateId);
        }
        else {
            List<CategoryDO> categoryDOS = null;
            try {
                categoryDOS = itemManager.selectCates(categoryQuery);
            } catch (DaoManagerException e) {
            }
            if (!CollectionUtils.isEmpty(categoryDOS)) {
                for (CategoryDO categoryDO : categoryDOS) {
                    cateIds.addAll(selectAllLeafCateIds(categoryDO.getId(), shopId));
                }
            }
        }
        return cateIds;
    }

    public boolean existSku(@NotNull Long itemId, @NotNull Long shopId) {
        String key = CacheKeyPrefix.ITEM_SET_THAT_HAS_SKU_OF_SHOP + shopId;
        return cacheService.sIsMember(key, itemId.toString());
    }

    public ServiceResult modifyCateShow(Long shopId, Long cateId, Integer show) {
        CategoryDO model = new CategoryDO();
        model.setShopId(shopId);
        model.setId(cateId);
        model.setShow(show);
        try {
            return ServiceResult.success(itemManager.saveCategory(model));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

    }
}

