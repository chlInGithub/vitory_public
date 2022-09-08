package com.chl.victory.web.controller.wm.coupon;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.activity.enums.OnlyUseEnum;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import lombok.Data;
import lombok.NonNull;
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

import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.shopCouponsFacade;

/**
 * 活动控制器
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p/wm/coupon/")
public class CouponController {

    @PostMapping(path = "save", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result save(@Validated CouponsInfoVO vo) {
        if (vo.getId() == null) {
            Long shopId = SessionUtil.getSessionCache().getShopId();
            ShopCouponsQueryDTO query = new ShopCouponsQueryDTO();
            query.setShopId(shopId);
            ServiceResult<Integer> result = shopCouponsFacade.count(query);
            if (result.getSuccess() && result.getData() != null) {
                accessLimitFacade
                        .doAccessLimit(shopId, result.getData(), AccessLimitTypeEnum.WM_SHOP_COUPON_TOTAL,
                                AccessLimitTypeEnum.WM_SHOP_COUPON_TOTAL.getDesc());
            }
        }

        ShopCouponsDTO model = CouponsInfoVO.transfer(vo);

        List<Long> itemIds = null;
        if (StringUtils.isNotBlank(vo.getItemIds())) {
            itemIds = Arrays.stream(vo.getItemIds().split(",")).filter(NumberUtils::isDigits).map(NumberUtils::toLong)
                    .collect(Collectors.toList());
        }
        ServiceResult serviceResult = shopCouponsFacade.save(model, itemIds);

        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        else {
            return Result.FAIL(serviceResult.getMsg());
        }
    }

    @PostMapping(path = "del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result del(@RequestParam("id") Long id) {
        @NotNull @Positive Long shopId = SessionUtil.getSessionCache().getShopId();
        ServiceResult serviceResult = shopCouponsFacade.del(id, shopId);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "valid", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result valid(@RequestParam("id") Long id) {
        Long shopId = SessionUtil.getSessionCache().getShopId();

        shopCouponsFacade.valid(id, shopId);

        return Result.SUCCESS();
    }

    @PostMapping(path = "invalid", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result invalid(@RequestParam("id") Long id) {
        Long shopId = SessionUtil.getSessionCache().getShopId();

        shopCouponsFacade.invalid(id, shopId);

        return Result.SUCCESS();
    }

    @GetMapping(path = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result detail(@RequestParam("id") Long itemId) {
        Long shopId = SessionUtil.getSessionCache().getShopId();
        ShopCouponsQueryDTO query = new ShopCouponsQueryDTO();
        query.setShopId(shopId);
        query.setId(itemId);
        ServiceResult<List<ShopCouponsDTO>> listServiceResult = shopCouponsFacade.select(query);

        if (!listServiceResult.getSuccess()) {
            return Result.FAIL(listServiceResult.getMsg());
        }
        if (CollectionUtils.isEmpty(listServiceResult.getData())) {
            return Result.SUCCESS();
        }

        ShopCouponsDTO model = listServiceResult.getData().get(0);

        List<Long> itemIds = LocalServiceManager.shopCouponsLocalService.getItemIdsOfCoupons(shopId, model.getId());

        CouponsInfoVO vo = CouponsInfoVO.transfer(model);
        if (!CollectionUtils.isEmpty(itemIds)) {
            vo.setItemIds(StringUtils.join(itemIds, ","));
        }

        return Result.SUCCESS(vo);
    }

    @GetMapping(path = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult list(@NonNull CouponsParam param) {
        ShopCouponsQueryDTO query = CouponsParam.stranfer(param);
        query.setOrderColumn("id");
        query.setDesc(true);
        // count
        ServiceResult<Integer> countResult = shopCouponsFacade.count(query);
        if (!countResult.getSuccess()) {
            return PageResult.FAIL(countResult.getMsg(), countResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), 0);
        }
        if (countResult.getData() == 0) {
            return PageResult.SUCCESS(Collections.EMPTY_LIST, param.getDraw(), param.getPageIndex(), 0);
        }
        // models
        ServiceResult<List<ShopCouponsDTO>> listResult = shopCouponsFacade.select(query);
        if (!listResult.getSuccess()) {
            return PageResult.FAIL(listResult.getMsg(), listResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), 0);
        }

        // 不对其中异常进行处理，因为数据正确的话应不出现异常
        List<CouponsInfoVO> itemInfos = listResult.getData().stream().filter(itemDO -> null != itemDO)
                .map(CouponsInfoVO::transfer).collect(Collectors.toList());

        return PageResult.SUCCESS(itemInfos, param.getDraw(), param.getPageIndex(), countResult.getData());
    }

    @Data
    public static class CouponsParam extends PageParam {

        Long id;

        String start;

        String end;

        String title;

        public static ShopCouponsQueryDTO stranfer(CouponsParam param) {
            ShopCouponsQueryDTO query = new ShopCouponsQueryDTO();
            BeanUtils.copyProperties(param, query);
            try {
                if (StringUtils.isNotBlank(param.getStart())) {
                    query.setStartValidTime(DateUtils.parseDate(param.getStart(), DateConstants.format1));
                }
                if (StringUtils.isNotBlank(param.getEnd())) {
                    query.setEndValidTime(DateUtils.parseDate(param.getEnd(), DateConstants.format1));
                }
            } catch (ParseException e) {
            }
            query.setShopId(SessionUtil.getSessionCache().getShopId());
            return query;
        }
    }

    @Data
    public static class CouponsInfoVO {

        Long id;

        /**
         * 标题
         */
        @NotEmpty(message = "请填写标题") String title;

        /**
         * 描述
         */
        @NotEmpty(message = "请填写描述") String desc;

        /**
         * 参与活动，需满足的金额
         */
        @NotNull(message = "请填写需满足的金额") @Positive(message = "请填写需满足的金额") BigDecimal meet;

        /**
         * 满足金额条件后，可优惠金额
         */
        @NotNull(message = "请填写可优惠金额") @Positive(message = "请填写可优惠金额") BigDecimal discount;

        /**
         * 生效时间
         */
        @NotEmpty(message = "请填写生效时间") String validTime;

        /**
         * 失效时间
         */
        @NotEmpty(message = "请填写失效时间") String invalidTime;

        @NotNull(message = "请填写状态") Integer status;

        @NotNull(message = "请填写排斥规则") Integer only;

        @NotNull(message = "请填写顺序") Integer order;

        /**
         * 参与活动的商品集合
         */
        String itemIds;

        String modifiedTime;

        public static ShopCouponsDTO transfer(CouponsInfoVO editInfo) {
            ShopCouponsDTO model = new ShopCouponsDTO();
            BeanUtils.copyProperties(editInfo, model);
            try {
                model.setValidTime(DateUtils.parseDate(editInfo.getValidTime(), DateConstants.format1));
                model.setInvalidTime(DateUtils.parseDate(editInfo.getInvalidTime(), DateConstants.format1));
            } catch (ParseException e) {
            }
            model.setOnly(Integer.valueOf(OnlyUseEnum.only.getCode()).equals(editInfo.getOnly()));
            model.setStatus(Integer.valueOf(ActivityConponsStatusEnum.valid.getCode()).equals(editInfo.getStatus()));
            model.setShopId(SessionUtil.getSessionCache().getShopId());
            model.setOperatorId(SessionUtil.getSessionCache().getUserId());
            model.setPriority(editInfo.getOrder().byteValue());
            return model;
        }

        public static CouponsInfoVO transfer(ShopCouponsDTO model) {
            CouponsInfoVO edit = new CouponsInfoVO();
            BeanUtils.copyProperties(model, edit);
            edit.setInvalidTime(DateFormatUtils.format(model.getInvalidTime(), DateConstants.format1));
            edit.setValidTime(DateFormatUtils.format(model.getValidTime(), DateConstants.format1));
            edit.setOnly(OnlyUseEnum.getByVal(model.getOnly()).getCode());
            edit.setStatus(ActivityConponsStatusEnum.getByVal(model.getStatus()).getCode());
            edit.setModifiedTime(DateFormatUtils.format(model.getModifiedTime(), DateConstants.format1));
            edit.setOrder(model.getPriority().intValue());
            return edit;
        }
    }
}
