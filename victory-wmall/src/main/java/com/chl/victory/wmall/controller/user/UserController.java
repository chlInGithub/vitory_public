package com.chl.victory.wmall.controller.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.model.UserCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;
import com.chl.victory.serviceapi.coupons.query.UserCouponsQueryDTO;
import com.chl.victory.serviceapi.share.model.ShareRecentDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.shareFacade;
import static com.chl.victory.webcommon.manager.RpcManager.shopCouponsFacade;
import static com.chl.victory.webcommon.manager.RpcManager.userCouponsFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/user/")
@Api(description = "用户相关信息")
@Validated
public class UserController {

    /**
     * 领取优惠券
     */
    @GetMapping("myShares")
    @ApiOperation(value = "我的分享", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result myShares() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        ServiceResult<List<ShareRecentDTO>> result = shareFacade.getShareRecents(sessionCache.getShopId(), sessionCache.getUserId());
        if (result.getSuccess()) {
            return Result.SUCCESS(result.getData());
        }
        return Result.FAIL(result.getMsg());
    }

    /**
     * 领取优惠券
     */
    @GetMapping("gainCoupon")
    @ApiOperation(value = "领取优惠券", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result gainCoupon(@RequestParam @ApiParam(name = "优惠券ID", required = true) Long couId) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        UserCouponsDTO userCouponsDTO = new UserCouponsDTO();
        userCouponsDTO.setShopId(sessionCache.getShopId());
        userCouponsDTO.setUserId(sessionCache.getUserId());
        userCouponsDTO.setCouponsId(couId);
        ServiceResult result = userCouponsFacade.gain(userCouponsDTO);
        if (result.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(result.getMsg());
    }

    /**
     * 所有在领取期内的优惠券
     * @return
     */
    @GetMapping("coupons")
    public Result coupons() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        UserCouponsQueryDTO userCouponsQuery = new UserCouponsQueryDTO();
        userCouponsQuery.setUserId(sessionCache.getUserId());
        userCouponsQuery.setShopId(sessionCache.getShopId());
        userCouponsQuery.setStatus(false);
        userCouponsQuery.setExpiryTime(new Date());
        ServiceResult<List<UserCouponsDTO>> userCouponsResult = userCouponsFacade.select(userCouponsQuery);
        if (!userCouponsResult.getSuccess()) {
            return Result.FAIL();
        }

        if (CollectionUtils.isEmpty(userCouponsResult.getData())) {
            return Result.SUCCESS();
        }

        List<UserCouponVO> vos = userCouponsResult.getData().stream().map(item -> {
            ShopCouponsQueryDTO query = new ShopCouponsQueryDTO();
            query.setShopId(sessionCache.getShopId());
            query.setEndValidTime(new Date());
            query.setId(item.getCouponsId());
            query.setStatus(ActivityConponsStatusEnum.valid.getVal());
            ServiceResult<List<ShopCouponsDTO>> serviceResult = shopCouponsFacade.select(query);
            if (!serviceResult.getSuccess() || CollectionUtils.isEmpty(serviceResult.getData())) {
                return null;
            }
            return UserCouponVO.transfer(item, serviceResult.getData().get(0));
        }).collect(Collectors.toList());

        return Result.SUCCESS(vos);
    }

    @Data
    public static class UserCouponVO {

        private Long id;

        private Long couponsId;

        private String title;

        private String validTime;

        private String invalidTime;

        private BigDecimal meet;

        private BigDecimal discount;

        public static UserCouponVO transfer(UserCouponsDTO userCoupons, ShopCouponsDTO shopCoupons) {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(shopCoupons, vo);
            vo.setValidTime(DateFormatUtils.format(shopCoupons.getValidTime(), DateConstants.format1));
            vo.setInvalidTime(DateFormatUtils.format(shopCoupons.getInvalidTime(), DateConstants.format1));
            vo.setId(userCoupons.getId());
            vo.setCouponsId(shopCoupons.getId());
            return vo;
        }
    }
}
