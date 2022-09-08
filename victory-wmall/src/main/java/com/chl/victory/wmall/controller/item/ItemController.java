package com.chl.victory.wmall.controller.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.item.enums.FreightStrategyEnum;
import com.chl.victory.serviceapi.item.enums.ItemStatusEnum;
import com.chl.victory.serviceapi.item.enums.NeedLogisticsEnum;
import com.chl.victory.serviceapi.item.enums.RangeTypeEnum;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.ItemLogisticsConfigDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;
import com.chl.victory.serviceapi.share.model.ShareDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.PageParam;
import com.chl.victory.wmall.model.PageResult;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.localservice.manager.LocalServiceManager.itemLocalService;
import static com.chl.victory.localservice.manager.LocalServiceManager.shopActivityLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;
import static com.chl.victory.webcommon.manager.RpcManager.shopActivityFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/item/")
@Api(description = "为商品详情、商品列表提供接口")
public class ItemController {

    @Resource
    CacheService cacheService;
    @Resource
    HttpServletResponse httpServletResponse;

    @GetMapping("detail")
    @ApiOperation(value = "商品详情", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result detail(@RequestParam @ApiParam(name = "商品ID", required = true) Long id) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        String key = CacheKeyPrefix.ITEM_DETAIL_OF_SHOP + sessionCache.getShopId();
        String field = id.toString();
        String cache = cacheService.hGet(key, field, String.class);
        if (StringUtils.isEmpty(cache)) {
            ItemQueryDTO itemQuery = new ItemQueryDTO();
            itemQuery.setShopId(sessionCache.getShopId());
            itemQuery.setId(id);
            itemQuery.setNeedFillCacheData(true);
            ServiceResult<List<ItemDTO>> itemServiceResult = itemFacade.selectItems(itemQuery);
            if (!itemServiceResult.getSuccess()) {
                return Result.FAIL(itemServiceResult.getMsg());
            }

            if (CollectionUtils.isEmpty(itemServiceResult.getData())) {
                return Result.FAIL("商品不存在");
            }

            ItemDTO itemDO = itemServiceResult.getData().get(0);

            SkuQueryDTO skuQuery = new SkuQueryDTO();
            skuQuery.setItemId(itemDO.getId());
            skuQuery.setShopId(sessionCache.getShopId());
            ServiceResult<List<SkuDTO>> skuResult = itemFacade.selectSkus(skuQuery);
            if (!skuResult.getSuccess()) {
                return Result.FAIL(skuResult.getMsg());
            }

            ItemVO itemVO = ItemVO.transfer(itemDO);

            if (!CollectionUtils.isEmpty(skuResult.getData())) {
                List<SkuVO> skuVOS = skuResult.getData().stream().map(skuDO -> SkuVO.transfer(skuDO))
                        .collect(Collectors.toList());
                itemVO.setSkus(skuVOS);
            }

            cache = JSONObject.toJSONString(itemVO);
            cacheService.hSet(key, field, cache, CacheExpire.SECONDS_30);
        }

        return Result.SUCCESS(cache);
    }

    @GetMapping("share")
    @ApiOperation(value = "商品分享", produces = MediaType.IMAGE_JPEG_VALUE)
    public void share(@RequestParam @ApiParam(name = "商品ID", required = true) Long id) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        ShareDTO shareParam = new ShareDTO();
        shareParam.setShopId(sessionCache.getShopId());
        shareParam.setAppId(sessionCache.getAppId());
        shareParam.setItemId(id);
        shareParam.setNick(sessionCache.getThirdNick());
        shareParam.setShopImg(sessionCache.getShopImg());
        shareParam.setShopName(sessionCache.getShopName());
        shareParam.setUserId(sessionCache.getUserId());
        shareParam.setUserImg(sessionCache.getThirdImg());
        ServiceResult<ShareDTO> serviceResult = RpcManager.shareFacade
                .shareItem(shareParam);
        byte[] bytes = serviceResult.getData().getImgBytes();
        try {
            httpServletResponse.setContentType("image/jpeg");
            httpServletResponse.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("evaluations")
    @ApiOperation(value = "商品评价列表，分页", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResult evaluations(EvaluationParam param) {
        // TODO
        Integer total = 20;
        List<EvaluationVO> evaluationVOS = mockEvaluationVOs();
        return PageResult.SUCCESS(evaluationVOS, null, param.getPageIndex(), total);
    }

    private List<EvaluationVO> mockEvaluationVOs() {
        List<EvaluationVO> evaluationVOS = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            EvaluationVO evaluationVO = new EvaluationVO();
            evaluationVO.setUserName("用户xxxx");
            evaluationVO.setContent("评价内容评价内容评价内容评价内容评价内容评价内容评价内容评价内容");
            evaluationVOS.add(evaluationVO);
        }
        return evaluationVOS;
    }

    /**
     * 商品列表
     * @return
     */
    @GetMapping("list")
    @ApiOperation(value = "商品列表")
    public PageResult list(ItemParam param) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        String key = CacheKeyPrefix.ITEM_LIST_OF_SHOP + sessionCache.getShopId();
        String field = JSONObject.toJSONString(param);
        String cache = cacheService.hGet(key, field, String.class);
        String totalKey = CacheKeyPrefix.ITEM_TOTAL_OF_SHOP + sessionCache.getShopId();
        Integer totalCache = cacheService.hGet(totalKey, field, Integer.class);
        if (StringUtils.isEmpty(cache)) {
            List<Long> itemIds = new ArrayList<>();
            if (param.type != null && RangeTypeEnum.getByCode(param.type) != null) {
                if (1 == param.type) {
                    itemIds.addAll(itemLocalService.getHotItemIds(sessionCache.getShopId()));
                }else {
                    if (RangeTypeEnum.getByCode(param.type) != null) {
                        itemIds.addAll(itemLocalService.getTypeItemIds(sessionCache.getShopId(), param.type));
                    }
                }
            }

            List<Long> ids = null;
            if (param.couponId != null) {
                List<Long> itemIdsOfCoupons = LocalServiceManager.shopCouponsLocalService.getItemIdsOfCoupons(sessionCache.getShopId(), param.getCouponId());
                if (!CollectionUtils.isEmpty(itemIdsOfCoupons)) {
                    ids = itemIdsOfCoupons;
                }
            }

            if (param.activityId != null) {
                List<Long> itemIdsOfActivity = shopActivityLocalService
                        .getItemIdsOfActivity(sessionCache.getShopId(), param.activityId);
                if (!CollectionUtils.isEmpty(itemIdsOfActivity)) {
                    ids = itemIdsOfActivity;
                }
            }

            List<Long> leafCateIds = null;
            if (!StringUtils.isEmpty(param.getCateId())) {
                leafCateIds = new ArrayList<>();
                leafCateIds.addAll(itemFacade.selectAllCachedLeafCateIds(param.getCateId(), sessionCache.getShopId()));
            }

            ItemQueryDTO itemQuery = new ItemQueryDTO();
            BeanUtils.copyProperties(param, itemQuery);
            itemQuery.setIds(ids);
            itemQuery.setLeafCateIds(leafCateIds);
            itemQuery.setShopId(sessionCache.getShopId());
            itemQuery.setNeedFillCacheData(true);
            if (itemQuery.getPageIndex() == null) {
                itemQuery.setPageIndex(0);
            }
            if (itemQuery.getPageSize() == null) {
                itemQuery.setPageSize(10);
            }
            if (!CollectionUtils.isEmpty(itemIds)) {
                itemQuery.setIds(itemIds);
                itemQuery.setPageSize(itemIds.size());
            }
            itemQuery.setJustOutline(true);
            itemQuery.setStatusStart(ItemStatusEnum.sale.getCode().byteValue());
            ServiceResult<List<ItemDTO>> itemServiceResult = itemFacade.selectItems(itemQuery);
            if (!itemServiceResult.getSuccess()) {
                return PageResult.FAIL(itemServiceResult.getMsg(), itemServiceResult.getFailType().getType(), 0,
                        param.getPageIndex(), 0);
            }

            int total = itemIds.size();
            if (0 == total) {
                ServiceResult<Integer> countResult = itemFacade.countItem(itemQuery);
                if (!countResult.getSuccess()) {
                    return PageResult
                            .FAIL(countResult.getMsg(), countResult.getFailType().getType(), 0, param.getPageIndex(),
                                    0);
                }
                total = countResult.getData();
            }

            List<ItemVO> itemVOS = itemServiceResult.getData().stream().map(itemDO -> {
                ItemVO vo = ItemVO.simpleTransfer(itemDO);
                return vo;
            }).collect(Collectors.toList());

            cache = JSONObject.toJSONString(itemVOS);
            totalCache = total;

            cacheService.hSet(key, field, cache, CacheExpire.SECONDS_30);
            cacheService.hSet(totalKey, field, totalCache, CacheExpire.SECONDS_30);
        }

        return PageResult.SUCCESS(cache, 0, param.getPageIndex(), totalCache);
    }

    @Data
    @ApiModel(value = "商品评价列表请求参数")
    public static class EvaluationParam extends PageParam {

        @ApiModelProperty(value = "商品ID", required = true)
        private Long itemId;
    }

    @Data
    public static class ItemParam extends PageParam {

        private Long id;

        private Long cateId;

        /**
         * 商品查询类型，如热卖 1、推荐 2
         */
        private Integer type;

        private String title;

        private Long couponId;

        private Long activityId;
    }

    @Data
    public static class ItemVO {

        private Long id;

        private String title;

        private String img;

        private List<String> imgs;

        private List<SkuVO> skus;

        private boolean existSku;

        /**
         * 一口价
         */
        private String price;

        private String labelPrice;

        /**
         * 发货地描述
         */
        private String from;

        /**
         * 快递费用描述
         */
        private String express;

        /**
         * 商品详情
         */
        private String detail;

        private Integer saleUV = 0;

        private Integer sales = 0;

        private Integer salesTotal = 0;

        private List<String> saleUsers;

        private Integer inventory;

        /**
         * 销售策略数据, json
         */
        List<String> saleStrategies;

        public static ItemVO transfer(ItemDTO itemDO) {
            ItemVO itemVO = new ItemVO();
            itemVO.setLabelPrice(itemDO.getTagPrice().toString());
            itemVO.setImgs(Arrays.stream(itemDO.getImgs().split(",")).collect(Collectors.toList()));
            itemVO.setImg(itemVO.getImgs().get(0));
            itemVO.setSaleStrategies(itemDO.getSaleStrategies());
            ItemLogisticsConfigDTO logisticsConfig = ItemLogisticsConfigDTO.getLogisticsConfig(itemDO.getLogistics());
            if (null != logisticsConfig) {
                itemVO.setFrom(logisticsConfig.getFrom());
                itemVO.setExpress(NeedLogisticsEnum.NEED.getCode().equals(logisticsConfig.getNeedLogistics()) ?
                        (FreightStrategyEnum.ZERO.getCode().equals(logisticsConfig.getFreightStrategy()) ?
                                FreightStrategyEnum.ZERO.getDesc() :
                                (FreightStrategyEnum.SAME.getCode().equals(logisticsConfig.getFreightStrategy()) ?
                                        (logisticsConfig.getSameFreightVal() == null ?
                                                "-" :
                                                logisticsConfig.getSameFreightVal()) + "元" :
                                        "--")) :
                        /*NeedLogisticsEnum.NOT_NEED.getDesc()*/
                        "自提");
                // TODO 预计到达时间
            }
            itemVO.setDetail(itemDO.getDetailHtml());
            itemVO.setPrice(itemDO.getPrice().toString());
            itemVO.setTitle(itemDO.getTitle());
            itemVO.setId(itemDO.getId());
            if (itemDO.getSaleUV() != null) {
                itemVO.setSaleUV(itemVO.getSaleUV() + itemDO.getSaleUV());
            }
            if (itemDO.getSales() != null) {
                itemVO.setSalesTotal(itemDO.getSales());
            }
            itemVO.setSaleUsers(itemDO.getSaleUsers());
            itemVO.setInventory(itemDO.getInventory());
            /*if (CollectionUtils.isEmpty(itemVO.getSaleUsers())) {
                itemVO.setSaleUsers(Arrays.asList(
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132",
                        "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83er7lBrMCuvxicC2ZjeI6ic9R6FKcV6Ud2drXfhJJ0gxenQWqjceFiayIKOqasiao85W1IibWib4Jt9RBuSQ/132"));
            }*/
            return itemVO;
        }

        public static ItemVO simpleTransfer(ItemDTO itemDO) {
            ItemVO itemVO = new ItemVO();
            itemVO.setLabelPrice(itemDO.getTagPrice().toString());
            itemVO.setImg(itemDO.getFirstImg());
            itemVO.setPrice(itemDO.getPrice().toString());
            itemVO.setTitle(itemDO.getTitle());
            itemVO.setId(itemDO.getId());
            itemVO.setSaleUV(itemDO.getSaleUV());
            itemVO.setSales(itemDO.getSales());
            itemVO.setSaleStrategies(itemDO.getSaleStrategies());
            if (null != itemDO.getSales()) {
                itemVO.setSalesTotal(itemDO.getSales());
            }
            itemVO.setInventory(itemDO.getInventory());
            itemVO.setExistSku(itemDO.isExistSku());
            return itemVO;
        }

    }

    @Data
    public static class SkuVO {

        Long id;

        String title;

        /**
         * 一口价
         */
        private String price;

        private String labelPrice;

        private Integer inventory;

        public static SkuVO transfer(SkuDTO skuDO) {
            SkuVO skuVO = new SkuVO();
            skuVO.setId(skuDO.getId());
            skuVO.setTitle(skuDO.getTitle());
            skuVO.setPrice(skuDO.getPrice().toString());
            skuVO.setInventory(skuDO.getInventory());
            if (null != skuDO.getTagPrice()) {
                skuVO.setLabelPrice(skuDO.getTagPrice().toString());
            }
            return skuVO;
        }
    }

    @Data
    private class EvaluationVO {

        private String userName;

        private String content;
    }
}
