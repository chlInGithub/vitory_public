package com.chl.victory.wmall.controller.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.enums.YesNoEnum;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.localservice.ShopActivityLocalService;
import com.chl.victory.localservice.ShopCouponsLocalService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.activity.model.ShopActivityDTO;
import com.chl.victory.serviceapi.activity.query.ShopActivityQueryDTO;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.model.UserCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;
import com.chl.victory.serviceapi.coupons.query.UserCouponsQueryDTO;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.query.CategoryQueryDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDeliveryAreaDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;
import static com.chl.victory.webcommon.manager.RpcManager.shopActivityFacade;
import static com.chl.victory.webcommon.manager.RpcManager.shopCouponsFacade;
import static com.chl.victory.webcommon.manager.RpcManager.userCouponsFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/shop/")
public class ShopController {

    @Resource
    ShopActivityLocalService shopActivityLocalService;

    @Resource
    ShopCouponsLocalService shopCouponsLocalService;

    /**
     * 用于页面header
     * @return
     */
    @RequestMapping("simple")
    @ApiOperation(value = "供页面header使用的店铺和用户简单信息", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result simple(@RequestParam(value = "needMoreShopInfo", required = false) Integer needMoreShopInfo) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        ShopVO shopVO;
        if (Integer.valueOf(1).equals(needMoreShopInfo)) {
            shopVO = new ShopVO(sessionCache.getShopId(), sessionCache.getShopName(), sessionCache.getShopImg(),
                    sessionCache.getMobile(), sessionCache.getShopAddress());
        }
        else {
            shopVO = new ShopVO(sessionCache.getShopId(), sessionCache.getShopName(), sessionCache.getShopImg(), null,
                    null);
        }
        UserShopVO userShopVO = new UserShopVO(
                new UserVO(sessionCache.getUserId(), sessionCache.getThirdNick(), sessionCache.getThirdImg(),
                        StringUtils.isEmpty(sessionCache.getMobile()) || sessionCache.getMobile().equals("null") ?
                                false :
                                true), shopVO);

        return Result.SUCCESS(userShopVO);
    }

    /**
     * 用于页面header
     * @return
     */
    @GetMapping("deliveryAreas")
    @ApiOperation(value = "店铺服务区域", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result deliveryAreas() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId = sessionCache.getShopId();

        ServiceResult<List<ShopDeliveryAreaDTO>> selectShopDeliveryAreas = merchantFacade
                .selectShopDeliveryArea(shopId);
        if (!selectShopDeliveryAreas.getSuccess()) {
            return Result.FAIL(selectShopDeliveryAreas.getMsg());
        }

        return Result.SUCCESS(selectShopDeliveryAreas.getData());
    }

    /**
     * 所有在活动期内的活动
     * @return
     */
    @GetMapping("acts")
    public Result activities(@RequestParam(value = "id", required = false) Long id) {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        String field = id == null ? "all" : id.toString();
        String activityCache = shopActivityLocalService.getActivityCache(sessionCache.getShopId(), field);
        if (StringUtils.isEmpty(activityCache)) {
            ShopActivityQueryDTO activityQuery = new ShopActivityQueryDTO();
            activityQuery.setShopId(sessionCache.getShopId());
            activityQuery.setEndValidTime(new Date());
            activityQuery.setStatus(ActivityConponsStatusEnum.valid.getVal());
            activityQuery.setId(id);
            ServiceResult<List<ShopActivityDTO>> activityReslut = shopActivityFacade.selectActivity(activityQuery);

            if (!activityReslut.getSuccess()) {
                return Result.FAIL(activityReslut.getMsg());
            }

            List<ActivityVO> vos = activityReslut.getData().stream()
                    .map(shopActivityDO -> ActivityVO.transfer(shopActivityDO, id != null))
                    .collect(Collectors.toList());
            activityCache = JSONObject.toJSONString(vos);
            shopActivityLocalService.setActivityCache(sessionCache.getShopId(), field, activityCache);
        }

        return Result.SUCCESS(activityCache);
    }

    /**
     * 所有在领取期内的优惠券
     * @return
     */
    @GetMapping("coupons")
    public Result coupons(@RequestParam(value = "id", required = false) Long id) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        Long shopId = sessionCache.getShopId();
        String field = sessionCache.getUserId() + CacheKeyPrefix.SEPARATOR + (id == null ? "all" : id.toString());
        String cache = shopCouponsLocalService.getCouponsCache(shopId, field);
        if (StringUtils.isEmpty(cache)) {
            ShopCouponsQueryDTO query = new ShopCouponsQueryDTO();
            query.setShopId(sessionCache.getShopId());
            query.setId(id);
            query.setEndValidTime(new Date());
            query.setStatus(ActivityConponsStatusEnum.valid.getVal());
            ServiceResult<List<ShopCouponsDTO>> serviceResult = shopCouponsFacade.select(query);

            if (!serviceResult.getSuccess()) {
                return Result.FAIL(serviceResult.getMsg());
            }

            List<CouponVO> vos = Collections.emptyList();
            if (!CollectionUtils.isEmpty(serviceResult.getData())) {
                Set<Long> gotCouponsIdSet = new HashSet<>();
                // 用户注册
                if (sessionCache.getUserId() != null) {
                    UserCouponsQueryDTO userCouponsQuery = new UserCouponsQueryDTO();
                    userCouponsQuery.setUserId(sessionCache.getUserId());
                    userCouponsQuery.setShopId(sessionCache.getShopId());
                    userCouponsQuery.setStatus(false);
                    userCouponsQuery.setExpiryTime(new Date());
                    userCouponsQuery.setCouponsId(id);
                    ServiceResult<List<UserCouponsDTO>> userCouponsResult = userCouponsFacade.select(userCouponsQuery);
                    if (!CollectionUtils.isEmpty(userCouponsResult.getData())) {
                        gotCouponsIdSet.addAll(userCouponsResult.getData().stream().map(UserCouponsDTO::getCouponsId)
                                .collect(Collectors.toSet()));
                    }
                }

                vos = serviceResult.getData().stream().map(shopCouponsDO -> {
                    Integer got = gotCouponsIdSet.contains(shopCouponsDO.getId()) ? 1 : 0;
                    CouponVO vo = CouponVO.transfer(shopCouponsDO);
                    vo.setGot(got);
                    return vo;
                }).collect(Collectors.toList());
            }

            cache = JSONObject.toJSONString(vos);
            shopCouponsLocalService.setCouponsCache(shopId, field, cache);
        }

        return Result.SUCCESS(cache);
    }

    /**
     * 所有首页展示类目
     * @return
     */
    @GetMapping("cates")
    public Result cates() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        CategoryQueryDTO categoryQuery = new CategoryQueryDTO();
        categoryQuery.setLevel((byte) 1);
        categoryQuery.setShopId(sessionCache.getShopId());
        categoryQuery.setShow(YesNoEnum.Yes.getCode());
        ServiceResult<List<CategoryDTO>> cateServiceResult = itemFacade.selectCates(categoryQuery);

        if (!cateServiceResult.getSuccess()) {
            return Result.FAIL(cateServiceResult.getMsg());
        }

        List<CateVO> cateVOS = cateServiceResult.getData().stream().filter(item -> !StringUtils.isEmpty(item.getImg()))
                .map(categoryDO -> new CateVO(categoryDO.getId(), categoryDO.getName(), categoryDO.getImg()))
                .collect(Collectors.toList());

        return Result.SUCCESS(cateVOS);
    }

    /**
     * 店铺支付类型配置
     * @return
     */
    @GetMapping(path = "payType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result payType() {
        ServiceResult<List<Integer>> shopResult = merchantFacade
                .selectShopPayType(SessionUtil.getSessionCache().getShopId());
        if (shopResult.getSuccess()) {
            List<PayTypeVO> payTypeVOs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(shopResult.getData())) {
                payTypeVOs.addAll(shopResult.getData().stream().map(item -> {
                    PayTypeEnum byCode = PayTypeEnum.getByCode(item);
                    return new PayTypeVO(byCode);
                }).collect(Collectors.toList()));
            }
            return Result.SUCCESS(payTypeVOs);
        }

        return Result.FAIL(shopResult.getMsg());
    }


    /**
     * 店铺交付类型配置
     * @return
     */
    @GetMapping(path = "deliveryType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result deliveryType() {
        ServiceResult<List<Integer>> shopResult = merchantFacade
                .selectShopDeliveryType(SessionUtil.getSessionCache().getShopId());
        if (shopResult.getSuccess()) {
            List<DeliveryTypeVO> payTypeVOs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(shopResult.getData())) {
                payTypeVOs.addAll(shopResult.getData().stream().map(item -> {
                    DeliverTypeEnum byCode = DeliverTypeEnum.getByCode(item);
                    return new DeliveryTypeVO(byCode);
                }).collect(Collectors.toList()));
            }
            return Result.SUCCESS(payTypeVOs);
        }

        return Result.FAIL(shopResult.getMsg());
    }

    @Data
    public static class PayTypeVO {

        Integer code;

        String desc;

        public PayTypeVO(PayTypeEnum payTypeEnum) {
            this.code = payTypeEnum.getCode();
            this.desc = payTypeEnum.getDesc();
        }
    }
    @Data
    public static class DeliveryTypeVO {

        Integer code;

        String desc;

        public DeliveryTypeVO(DeliverTypeEnum deliverTypeEnum) {
            if (null == deliverTypeEnum) {
                return;
            }
            this.code = deliverTypeEnum.getCode();
            this.desc = deliverTypeEnum.equals(DeliverTypeEnum.noLogistics) ? "自提" : "快递/配送";
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserShopVO {

        private UserVO user;

        private ShopVO shop;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShopVO {

        private Long id;

        private String name;

        private String img;

        private String mobile;

        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserVO {

        private Long id;

        private String name;

        private String img;

        private boolean hasPhone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityVO {

        private Long id;

        private String name;

        private String desc;

        private String img;

        private Integer type;

        private String validTime;

        private String invalidTime;

        public static ActivityVO transfer(ShopActivityDTO model, boolean allInfo) {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(model, vo);
            vo.setName(model.getTitle());
            vo.setType(0);
            vo.setValidTime(DateFormatUtils.format(model.getValidTime(), DateConstants.format1));
            vo.setInvalidTime(DateFormatUtils.format(model.getInvalidTime(), DateConstants.format1));
            return vo;
        }
    }

    @Data
    public static class CouponVO {

        private Long id;

        private String name;

        /**
         * 0 未领取，1 已领取
         */
        private Integer got;

        private String validTime;

        private String invalidTime;

        private BigDecimal meet;

        private BigDecimal discount;

        public static CouponVO transfer(ShopCouponsDTO model) {
            CouponVO vo = new CouponVO();
            BeanUtils.copyProperties(model, vo);
            vo.setName(model.getTitle());
            vo.setValidTime(DateFormatUtils.format(model.getValidTime(), DateConstants.format1));
            vo.setInvalidTime(DateFormatUtils.format(model.getInvalidTime(), DateConstants.format1));
            return vo;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CateVO {

        private Long id;

        private String name;

        private String img;
    }
}
