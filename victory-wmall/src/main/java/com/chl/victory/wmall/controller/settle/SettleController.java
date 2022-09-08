package com.chl.victory.wmall.controller.settle;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.localservice.CartLocalService;
import com.chl.victory.localservice.OrderLocalService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceapi.order.model.SettleDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.controller.order.OrderController.OrderInfo4Create;
import com.chl.victory.wmall.controller.user.DeliverController.ReceiveInfoVO;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@Slf4j
@RestController
@RequestMapping("/wmall/settle/")
@Api(description = "为结算页面提供接口")
public class SettleController {

    @Resource
    CartLocalService cartLocalService;

    @GetMapping("compute")
    @ApiOperation(value = "计算结算信息，返回包括商品、金额、收货人等", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result compute(@Validated OrderInfo4Create orderInfo4Create) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        accessLimitFacade.checkAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_ORDER_COMPUTE,
                sessionCache.getThirdId() + CacheKeyPrefix.SEPARATOR + sessionCache.getUserId(), null);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        BeanUtils.copyProperties(orderInfo4Create, orderCreateDTO);
        orderCreateDTO.setShopId(sessionCache.getShopId());
        orderCreateDTO.setBuyerId(sessionCache.getUserId());

        // 检查购物车是否存在该商品
        boolean inCart = cartLocalService
                .checkInCart(sessionCache.getShopId(), sessionCache.getUserId(), orderCreateDTO.getItems());
        if (!inCart) {
            return Result.FAIL("商品不在购物车中");
        }

        ServiceResult<SettleDTO> settleDTOServiceResult = orderFacade.computeForSettle(orderCreateDTO);

        if (!settleDTOServiceResult.getSuccess()) {
            return Result.FAIL(settleDTOServiceResult.getMsg());
        }

        if (null == settleDTOServiceResult.getData()) {
            return Result.FAIL("计算结算信息失败");
        }

        SettleVO settleVO;
        try {
            settleVO = SettleVO.transfer(settleDTOServiceResult.getData(), orderCreateDTO);
        } catch (Exception e) {
            SettleController.log.error("结果转换异常{}", orderCreateDTO, e);
            return Result.FAIL("结果转换异常");
        }

        accessLimitFacade.incrAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_ORDER_COMPUTE,
                sessionCache.getThirdId() + CacheKeyPrefix.SEPARATOR + sessionCache.getUserId());

        return Result.SUCCESS(settleVO);
    }

    @Data
    public static class SettleParam {

        @NotEmpty(message = "缺少商品参数") List<String> items;

        /**
         * 手动选定的优惠券
         */
        String couId;

        String actId;

        /**
         * 手动选定收货信息
         */
        @NotBlank(message = "缺少收货地址ID") String deliverId;
    }

    @Data
    public static class SettleVO {

        /**
         * 优惠金额
         */
        private String coupon;

        /**
         * 优惠后总金额(包括运费)
         */
        private String total;

        /**
         * 商品数量
         */
        private Integer itemCount;

        /**
         * 商品信息
         */
        private List<SettleItemVO> itemList;

        /**
         * 选用的收货地址
         */
        private ReceiveInfoVO usedReceiveInfo;

        /**
         * 预估到达天数
         */
        private Integer estimatedDatesBeforeReceive;

        /**
         * 快递描述，包括金额
         */
        private String deliverDesc;

        private String freight;

        /**
         * 优惠详情
         */
        private List<SettleCouponVO> couponDetailList;

        public static SettleVO transfer(SettleDTO settleDTO, OrderCreateDTO orderCreateDTO) throws Exception {
            SettleVO settleVO = new SettleVO();

            List<SettleCouponVO> settleCouponVOs = new ArrayList<>();

            if (settleDTO.getActivityDTO() != null) {
                SettleCouponVO settleCouponVO = new SettleCouponVO();
                settleCouponVO.setName(settleDTO.getActivityDTO().getDesc());
                settleCouponVO.setCoupon(settleDTO.getActivityDTO().getTotalDis());
                settleCouponVOs.add(settleCouponVO);
            }
            if (settleDTO.getCouponsDTO() != null) {
                SettleCouponVO settleCouponVO = new SettleCouponVO();
                settleCouponVO.setName(settleDTO.getCouponsDTO().getDesc());
                settleCouponVO.setCoupon(settleDTO.getCouponsDTO().getDiscount());
                settleCouponVOs.add(settleCouponVO);
            }

            settleVO.setCouponDetailList(settleCouponVOs);

            List<SettleItemVO> settleItemVOS = new ArrayList<>();
            for (OrderLocalService.ItemOfNewOrder itemOfNewOrder : OrderLocalService
                    .parseItemsOfNewOrder(orderCreateDTO.getItems())) {
                SettleItemVO settleItemVO = new SettleItemVO();
                settleItemVO.setItemId(itemOfNewOrder.getItemId());
                settleItemVO.setSkuId(itemOfNewOrder.getSkuId());

                ItemDTO itemDO = settleDTO.getItemIdMap().get(itemOfNewOrder.getItemId());
                settleItemVO.setTitle(itemDO.getTitle());
                settleItemVO.setSkuTitle(itemDO.getSubTitle());
                settleItemVO.setPrice(itemDO.getPrice().toString());
                settleItemVO.setImg(itemDO.getFirstImg());

                if (!ShopConstants.SKU_ID_WHEN_NO_SKU.equals(itemOfNewOrder.getSkuId())) {
                    SkuDTO skuDO = settleDTO.getSkuIdMap().get(itemOfNewOrder.getSkuId());
                    if (!StringUtils.isEmpty(skuDO.getTitle())) {
                        settleItemVO.setSkuTitle(skuDO.getTitle());
                    }
                    /*if (!StringUtils.isEmpty(skuDO.getSubTitle())) {
                        settleItemVO.setSkuTitle(skuDO.getSubTitle());
                    }*/
                    settleItemVO.setPrice(skuDO.getPrice().toString());
                    if (!StringUtils.isEmpty(skuDO.getFirstImg())) {
                        settleItemVO.setImg(skuDO.getFirstImg());
                    }
                }
                settleItemVO.setCount(itemOfNewOrder.getCount());
                settleItemVOS.add(settleItemVO);
            }
            settleVO.setItemList(settleItemVOS);

            if (DeliverTypeEnum.noLogistics.equals(settleDTO.getOrderDeliverDTO().getType())) {
                settleVO.setDeliverDesc("自提");
            }
            else {
                settleVO.setDeliverDesc("快递");
                settleVO.setFreight(settleDTO.getOrderDeliverDTO().getFreight().toString());
            }
            settleVO.setEstimatedDatesBeforeReceive(0);
            ReceiveInfoVO receiveInfoVO = new ReceiveInfoVO();
            receiveInfoVO.setId(settleDTO.getOrderDeliverDTO().getId());
            receiveInfoVO.setName(settleDTO.getOrderDeliverDTO().getName());
            receiveInfoVO.setMobile(settleDTO.getOrderDeliverDTO().getMobile().toString());
            receiveInfoVO.setCity(settleDTO.getOrderDeliverDTO().getCity());
            receiveInfoVO.setAddress(settleDTO.getOrderDeliverDTO().getAddress());
            settleVO.setUsedReceiveInfo(receiveInfoVO);

            settleVO.setCoupon(settleDTO.getTotalFee().add(settleDTO.getOrderDeliverDTO().getFreight())
                    .subtract(settleDTO.getRealFee()).toString());
            settleVO.setTotal(settleDTO.getRealFee().toString());
            settleVO.setItemCount(settleDTO.getItemCount());

            return settleVO;
        }
    }

    @Data
    public static class SettleCouponVO {

        private String name;

        /**
         * 优惠金额
         */
        private String coupon;
    }

    @Data
    public static class SettleItemVO {

        private Long itemId;

        private Long skuId;

        private String title;

        private String skuTitle;

        private String price;

        private Integer count;

        private String img;
    }
}
