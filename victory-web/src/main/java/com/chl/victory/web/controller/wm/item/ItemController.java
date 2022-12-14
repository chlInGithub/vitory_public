package com.chl.victory.web.controller.wm.item;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSON;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.item.enums.RangeTypeEnum;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.item.enums.ItemStatusEnum;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.localservice.manager.LocalServiceManager.itemLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;

/**
 * ???????????????
 * @author hailongchen9
 */
@RestController
@RequestMapping("/p/wm/item/")
public class ItemController {

    /**
     * @param type {@link com.chl.victory.serviceapi.item.enums.RangeTypeEnum}
     * @param itemIds
     * @return
     * @throws ParseException
     */
    @PostMapping(path = "saveIdsOfType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveIdsOfType(@RequestParam("type") Integer type, @RequestParam(name = "itemIds", required = false) String itemIds) throws ParseException {
        RangeTypeEnum rangeTypeEnum = RangeTypeEnum.getByCode(type);
        if (null == rangeTypeEnum) {
            return Result.FAIL("type error");
        }

        List<Long> itemIdList = null;
        if (StringUtils.isNotBlank(itemIds)) {
            itemIdList = Arrays.stream(itemIds.split(",")).filter(NumberUtils::isDigits).map(NumberUtils::toLong)
                    .collect(Collectors.toList());
        }

        Long shopId = SessionUtil.getSessionCache().getShopId();

        itemLocalService.setTypeItemIds(shopId, type, itemIdList);

        return Result.SUCCESS();
    }

    @GetMapping(path = "getIdsOfType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result getIdsOfType(@RequestParam("type") Integer type) throws ParseException {
        RangeTypeEnum rangeTypeEnum = RangeTypeEnum.getByCode(type);
        if (null == rangeTypeEnum) {
            return Result.FAIL("type error");
        }

        Long shopId = SessionUtil.getSessionCache().getShopId();
        List<Long> itemIdList = itemLocalService.getTypeItemIds(shopId, type);

        return Result.SUCCESS(StringUtils.join(itemIdList, ","));
    }

    @PostMapping(path = "save", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result save(@Validated ItemInfo4Edit itemEditInfo) throws ParseException {

        if (itemEditInfo.getId() == null) {
            Long shopId = SessionUtil.getSessionCache().getShopId();
            ItemQueryDTO itemQuery = new ItemQueryDTO();
            itemQuery.setShopId(shopId);
            ServiceResult<Integer> result = itemFacade.countItem(itemQuery);
            if (result.getSuccess() && result.getData() != null) {
                accessLimitFacade.doAccessLimit(shopId, result.getData(), AccessLimitTypeEnum.WM_SHOP_ITEM_TOTAL,
                        AccessLimitTypeEnum.WM_SHOP_ITEM_TOTAL.getDesc());
            }
        }

        ItemDTO itemDO = ItemInfo4Edit.transfer(itemEditInfo);
        List<SkuDTO> skuDOS = ItemInfo4Edit.transfer2SkuDO(itemEditInfo.getSkus());

        ServiceResult serviceResult = itemFacade.save(itemDO, skuDOS);

        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        else {
            return Result.FAIL(serviceResult.getMsg());
        }
    }

    @PostMapping(path = "updateStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result updateStatus(@RequestParam("ids") Long[] itemIds, @RequestParam("status") Integer status) {
        if (itemIds.length == 0 || ItemStatusEnum.getByCode(status) == null) {
            return Result.FAIL(ServiceFailTypeEnum.PARAM_INVALID.getDesc());
        }

        // ???????????????????????????????????????builder
        StringBuilder errBuilder = new StringBuilder();
        for (Long itemId : itemIds) {
            ServiceResult serviceResult = itemFacade
                    .updateStatus(itemId, status, SessionUtil.getSessionCache().getShopId());
            if (!serviceResult.getSuccess()) {
                errBuilder.append(itemId).append(":").append(serviceResult.getMsg()).append(";");
            }
        }
        return Result.SUCCESS(errBuilder.toString());
    }

    /**
     * ids=1,2,3,4
     */
    @PostMapping(path = "del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result del(@RequestParam("ids") Long[] itemIds) {
        ServiceResult serviceResult = itemFacade
                .delItem(SessionUtil.getSessionCache().getShopId(), Arrays.asList(itemIds));
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ??????
     * ids=1,2,3,4
     */
    @PostMapping(path = "shelf", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result shelf(@RequestParam("ids") Long[] itemIds) {
        Long shopId = SessionUtil.getSessionCache().getShopId();
        if (ArrayUtils.isEmpty(itemIds)) {
            return Result.SUCCESS();
        }

        for (Long itemId : itemIds) {
            itemFacade.shelf(shopId, itemId);
        }

        return Result.SUCCESS();
    }

    /**
     * ??????
     * ids=1,2,3,4
     */
    @PostMapping(path = "soldOut", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result soldOut(@RequestParam("ids") Long[] itemIds) {
        Long shopId = SessionUtil.getSessionCache().getShopId();
        if (ArrayUtils.isEmpty(itemIds)) {
            return Result.SUCCESS();
        }

        for (Long itemId : itemIds) {
            itemFacade.soldOut(shopId, itemId);
        }

        return Result.SUCCESS();
    }

    @GetMapping(path = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result itemDetail(@RequestParam("id") Long itemId) {
        ItemQueryDTO itemQuery = new ItemQueryDTO();
        itemQuery.setShopId(SessionUtil.getSessionCache().getShopId());
        itemQuery.setId(itemId);
        ServiceResult<List<ItemDTO>> listServiceResult = itemFacade.selectItems(itemQuery);

        if (!listServiceResult.getSuccess()) {
            return Result.FAIL(listServiceResult.getMsg());
        }
        if (CollectionUtils.isEmpty(listServiceResult.getData())) {
            return Result.SUCCESS();
        }

        ItemDTO itemDO = listServiceResult.getData().get(0);

        SkuQueryDTO skuQuery = new SkuQueryDTO();
        skuQuery.setItemId(itemId);
        skuQuery.setShopId(itemQuery.getShopId());
        ServiceResult<List<SkuDTO>> skuResult = itemFacade.selectSkus(skuQuery);
        if (!skuResult.getSuccess()) {
            return Result.FAIL(skuResult.getMsg());
        }

        ItemInfo4Edit itemInfo4Edit = ItemInfo4Edit.transfer(itemDO, skuResult.getData());

        return Result.SUCCESS(itemInfo4Edit);
    }

    @GetMapping(path = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult itemList(@NonNull ItemParam param) {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ItemQueryDTO itemQuery = ItemParam.stranfer(param);
        itemQuery.setOrderColumn("id");
        itemQuery.setDesc(true);
        if (!CollectionUtils.isEmpty(itemQuery.getIds())) {
            itemQuery.setPageIndex(null);
        }
        Long shopId = itemQuery.getShopId();

        if (param.getCateId() != null && !param.getCateId().equals(ShopConstants.CATE_NO_VAL)) {
            itemQuery.setLeafCateIds(itemFacade.selectAllCachedLeafCateIds(param.getCateId(), sessionCache.getShopId()));
        }
        // count
        Integer count = 0;
        Byte status = itemQuery.getStatus();
        if (null != status && (status.equals(ItemStatusEnum.sellOut.getCode().byteValue())
                || status.equals(ItemStatusEnum.warn.getCode().byteValue()))) {
            if (status.equals(ItemStatusEnum.sellOut.getCode().byteValue())) {
                count = itemLocalService.countSellOut(shopId).intValue();
                if (count > 0) {
                    itemQuery.setIds(itemLocalService.getSellOut(shopId));
                    itemQuery.setStatus(null);
                }
            }else if (status.equals(ItemStatusEnum.warn.getCode().byteValue())) {
                count = itemLocalService.countLowInventory(shopId).intValue();
                if (count > 0) {
                    itemQuery.setIds(itemLocalService.getLowInventory(shopId));
                    itemQuery.setStatus(null);
                }
            }
        }else {
            ServiceResult<Integer> countResult = itemFacade.countItem(itemQuery);
            if (!countResult.getSuccess()) {
                return PageResult.FAIL(countResult.getMsg(), countResult.getFailType().getType(), param.getDraw(),
                        param.getPageIndex(), 0);
            }
            count = countResult.getData();
        }

        if (count == 0) {
            return PageResult.SUCCESS(Collections.EMPTY_LIST, param.getDraw(), param.getPageIndex(), 0);
        }

        // items
        itemQuery.setNeedFillCacheData(true);
        ServiceResult<List<ItemDTO>> itemsResult = itemFacade.selectItems(itemQuery);
        if (!itemsResult.getSuccess()) {
            return PageResult.FAIL(itemsResult.getMsg(), itemsResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), count);
        }
        // ???????????????????????????????????????????????????????????????????????????
        List<ItemInfo4List> itemInfos = itemsResult.getData().stream().filter(itemDO -> null != itemDO).map(itemDO -> {
            // sku
            SkuQueryDTO skuQuery = new SkuQueryDTO();
            skuQuery.setItemId(itemDO.getId());
            skuQuery.setShopId(SessionUtil.getSessionCache().getShopId());
            ServiceResult<List<SkuDTO>> skusResult = itemFacade.selectSkus(skuQuery);
            // category
            CategoryDTO categoryDO = null;
            /*CategoryQuery categoryQuery = new CategoryQuery();
            categoryQuery.setShopId(itemDO.getShopId());
            categoryQuery.setId(itemDO.getLeafCateId());
            ServiceResult<List<CategoryDO>> cateResult = itemService.selectCates(categoryQuery);
            categoryDO = cateResult.getData().get(0);*/

            return ItemInfo4List.transfer(itemDO, categoryDO, skusResult.getData());
        }).collect(Collectors.toList());

        return PageResult.SUCCESS(itemInfos, param.getDraw(), param.getPageIndex(),
                param.getTotal() == null ? count : param.getTotal());
    }

    @GetMapping(path = "spaceCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result statusCount(ItemParam param) {
        ItemQueryDTO itemQuery = ItemParam.stranfer(param);
        List<ItemSpaceAndCount> itemSpaceAndCounts = new ArrayList<>();
        for (ItemSpace itemSpace : ItemSpace.values()) {
            switch (itemSpace) {
                case sellOut:
                {
                    Long countSellOut = itemLocalService.countSellOut(itemQuery.getShopId());
                    itemSpaceAndCounts.add(new ItemSpaceAndCount(itemSpace.desc, countSellOut.intValue()));
                    break;
                }
                case warn:
                {
                    Long count = itemLocalService.countLowInventory(itemQuery.getShopId());
                    itemSpaceAndCounts.add(new ItemSpaceAndCount(itemSpace.desc, count.intValue()));
                    break;
                }
                default:
                {
                    itemQuery.setStatus(itemSpace.status != null ? itemSpace.status.byteValue() : null);
                    ServiceResult<Integer> countResult = itemFacade.countItem(itemQuery);
                    itemSpaceAndCounts.add(new ItemSpaceAndCount(itemSpace.desc, countResult.getData()));
                }
            }
        }

        return Result.SUCCESS(itemSpaceAndCounts);
    }

    enum ItemSpace {
        all(null, "all"), published(ItemStatusEnum.published.getCode(), "published"), willShelf(
                ItemStatusEnum.willShelf.getCode(), "willShelf"), sale(ItemStatusEnum.sale.getCode(), "sale"), sellOut(
                ItemStatusEnum.sellOut.getCode(), "sellOut"), warn(ItemStatusEnum.warn.getCode(), "warn");

        Integer status;

        String desc;

        ItemSpace(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }

    @Data
    public static class ItemParam extends PageParam {

        String ids;

        Long id;

        /*String start;
        String end;*/ String title;

        Long cateId;

        /**
         * ????????????ID
         * @see ItemStatusEnum
         */
        Integer space;

        Integer total;

        Integer presell;

        public static ItemQueryDTO stranfer(ItemParam param) {
            ItemQueryDTO itemQuery = new ItemQueryDTO();
            BeanUtils.copyProperties(param, itemQuery);
            if (StringUtils.isNotBlank(param.getIds())) {
                List<Long> collect = Arrays.stream(param.getIds().split(",")).filter(NumberUtils::isDigits)
                        .map(NumberUtils::toLong).collect(Collectors.toList());
                itemQuery.setIds(collect);
            }
            // itemQuery.setLeafCateId(ItemService.CATE_NO_VAL.equals(param.getCateId()) ? null : param.getCateId());
            itemQuery.setStatus(param.getSpace() != null ? param.getSpace().byteValue() : null);
            itemQuery.setShopId(SessionUtil.getSessionCache().getShopId());
            return itemQuery;
        }
    }

    @Data
    public static class ItemSpaceAndCount {

        String space;

        Integer count;

        public ItemSpaceAndCount(String space, Integer count) {
            this.space = space;
            this.count = count;
        }

    }

    /**
     * ??????list?????????iteminfo
     */
    @Data
    public static class ItemInfo4List {

        /**
         * ??????ID
         */
        String id;

        /**
         * ????????????
         */
        String title;

        /**
         * ????????????url
         */
        String img;

        /**
         * ??????????????????
         */
        String modifyTime;

        List<Sku> skus;

        /**
         * ????????????
         */
        String cate;

        /**
         * sku????????????
         */
        Boolean samePrice;

        /**
         * ?????????
         */
        String price;

        /**
         * ??????
         */
        Integer sales;

        /**
         * ?????????
         */
        Integer inventory;

        /**
         * ???????????????c?????????
         */
        Integer sort;

        /**
         * ????????????
         */
        Integer status;

        String statusDesc;

        /**
         * ????????????????????????
         */
        Integer space;

        /**
         * ??????????????????, json
         */
        List<String> saleStrategies;

        public static ItemInfo4List transfer(ItemDTO itemDO, CategoryDTO categoryDO, List<SkuDTO> skuDOs) {
            ItemInfo4List itemInfo4List = transfer(itemDO, categoryDO);
            itemInfo4List.setSkus(ItemInfo4Edit.transfer2Sku(skuDOs));
            return itemInfo4List;
        }

        private static ItemInfo4List transfer(ItemDTO itemDO, CategoryDTO categoryDO) {
            ItemInfo4List itemInfo4List = new ItemInfo4List();
            BeanUtils.copyProperties(itemDO, itemInfo4List);
            itemInfo4List.setImg(itemDO.getFirstImg());
            itemInfo4List.setModifyTime(DateFormatUtils.format(itemDO.getModifiedTime(), DateConstants.format1));
            itemInfo4List.setCate(categoryDO != null ? categoryDO.getName() : "");
            itemInfo4List.setPrice(itemDO.getPrice().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
            itemInfo4List.setId(itemDO.getId().toString());
            itemInfo4List.setSpace(itemDO.getStatus().intValue());
            itemInfo4List.setStatusDesc(ItemStatusEnum.getByCode(itemDO.getStatus().intValue()).getDesc());
            return itemInfo4List;
        }
    }

    @Data
    public static class Sku {

        public String id;

        @NotEmpty(message = "?????????SKU??????")
        public String title;

        /**
         * ?????????
         */
        @NotNull(message = "?????????SKU?????????")
        @Positive(message = "?????????SKU?????????")
        public BigDecimal price;

        public Integer sales;

        public Integer inventory;

        public static SkuDTO transfer(Sku sku) {
            SkuDTO skuDO = new SkuDTO();
            BeanUtils.copyProperties(sku, skuDO);
            skuDO.setId(NumberUtils.isDigits(sku.getId()) ? NumberUtils.toLong(sku.getId()) : null);
            skuDO.setShopId(SessionUtil.getSessionCache().getShopId());
            skuDO.setOperatorId(SessionUtil.getSessionCache().getUserId());
            return skuDO;
        }

        public static Sku transfer(SkuDTO skuDO) {
            Sku sku = new Sku();
            BeanUtils.copyProperties(skuDO, sku);
            sku.setId(skuDO.getId().toString());
            return sku;
        }
    }

    /**
     * ??????????????????????????????iteminfo
     */
    @Data
    public static class ItemInfo4Edit {

        Long id;

        /**
         * ??????ID
         */
        @NotNull(message = "???????????????ID") @Positive(message = "???????????????ID") Long cateId;

        /**
         * ????????????
         */
        String outerNo;

        /**
         * ??????
         */
        @NotEmpty(message = "???????????????") String title;

        /**
         * ?????????
         */
        String subTitle;

        /**
         * ?????????
         */
        String key;

        /**
         * ??????
         */
        BigDecimal cost;

        /**
         * ?????????
         */
        @NotNull(message = "??????????????????") @Positive(message = "??????????????????") BigDecimal labelPrice;

        /**
         * ?????????
         */
        @NotNull(message = "??????????????????") @Positive(message = "??????????????????") BigDecimal price;

        /**
         * ??????
         */
        @Positive(message = "???????????????") Integer inventory;

        /**
         * ??????
         */
        @Positive(message = "???????????????") Integer sales;

        Attribute attr;

        /**
         * ???????????????????????????,??????????????????
         */
        @NotEmpty(message = "?????????????????????") String imgs;

        List<Sku> skus;

        /**
         * ????????????
         */
        @NotNull(message = "?????????????????????") LogisticsConfig logistics;

        /**
         * ??????html??????
         */
        @NotEmpty(message = "?????????????????????") String detail;

        public static ItemDTO transfer(ItemInfo4Edit itemEditInfo) throws ParseException {
            ItemDTO itemDO = new ItemDTO();
            BeanUtils.copyProperties(itemEditInfo, itemDO);
            itemDO.setDetailHtml(itemEditInfo.getDetail());
            itemDO.setLeafCateId(itemEditInfo.getCateId());
            itemDO.setTagPrice(itemEditInfo.getLabelPrice());
            itemDO.setLogistics(JSON.toJSONString(itemEditInfo.getLogistics()));
            itemDO.setAttr(JSON.toJSONString(itemEditInfo.getAttr()));
            itemDO.setShopId(SessionUtil.getSessionCache().getShopId());
            itemDO.setOperatorId(SessionUtil.getSessionCache().getUserId());
            return itemDO;
        }

        public static List<SkuDTO> transfer2SkuDO(List<Sku> skus) {
            if (!CollectionUtils.isEmpty(skus)) {
                return skus.stream().filter(sku -> null != sku).map(Sku::transfer).collect(Collectors.toList());
            }
            return null;
        }

        public static List<Sku> transfer2Sku(List<SkuDTO> skuDOs) {
            if (!CollectionUtils.isEmpty(skuDOs)) {
                return skuDOs.stream().filter(sku -> null != sku).map(Sku::transfer).collect(Collectors.toList());
            }
            return null;
        }

        public static ItemInfo4Edit transfer(ItemDTO itemDO, List<SkuDTO> skus) {
            ItemInfo4Edit itemInfo4Edit = new ItemInfo4Edit();
            BeanUtils.copyProperties(itemDO, itemInfo4Edit);
            itemInfo4Edit.setDetail(itemDO.getDetailHtml());
            itemInfo4Edit.setCateId(itemDO.getLeafCateId());
            itemInfo4Edit.setLabelPrice(itemDO.getTagPrice());
            itemInfo4Edit.setLogistics(JSON.parseObject(itemDO.getLogistics(), LogisticsConfig.class));
            itemInfo4Edit.setAttr(JSON.parseObject(itemDO.getAttr(), Attribute.class));
            if (!CollectionUtils.isEmpty(skus)) {
                itemInfo4Edit.setSkus(transfer2Sku(skus));
            }
            return itemInfo4Edit;
        }
    }

    /**
     * ?????????????????????
     */
    @Data
    public static class LogisticsConfig {

        /**
         * ???????????????0???????????? 1 ??????
         */
        Integer needLogistics;

        /**
         * ???????????? 0???????????? 1?????????????????????
         */
        Integer freightStrategy;

        /**
         * for 1????????????
         */
        Integer sameFreightVal;
    }

    /**
     * ????????????
     */
    @Data
    public static class Attribute {

        /**
         * ???????????????KG
         */
        BigDecimal weight;

        /**
         * ????????????????????????
         */
        BigDecimal volume;
    }
}
