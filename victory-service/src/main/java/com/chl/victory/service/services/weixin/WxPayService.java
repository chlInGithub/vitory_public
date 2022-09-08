package com.chl.victory.service.services.weixin;

import javax.validation.constraints.NotNull;

import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.weixin.model.WXRequestPaymentParam;
import com.chl.victory.serviceapi.weixin.model.pay.WxPrePayDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.itemService;
import static com.chl.victory.service.services.ServiceManager.merchantService;
import static com.chl.victory.service.services.ServiceManager.orderService;
import static com.chl.victory.service.services.ServiceManager.weixinMiniProgramService;

/**
 * @author ChenHailong
 * @date 2020/8/31 14:13
 **/
@Service
public class WxPayService {

    public ServiceResult<Boolean> checkPayed(Long shopId, Long userId, String appId, Long orderId) throws Exception {

        // 查询shop
        ShopDO shopDO = merchantService.selectShop(shopId);
        ShopAppDO weixinConfig = merchantService.getShopAppCache(shopId, appId);

        // 查询订单
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setBuyerId(userId);
        orderServiceQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());
        boolean payed = false;
        try {
            OrderDTO orderDTO;
            orderDTO = orderService.selectMainOrder(orderServiceQuery);

            if (orderDTO != null && OrderStatusEnum.newed.equals(orderDTO.getStatus())) {
                payed = weixinMiniProgramService.checkPayedViaWXSDK(shopDO, weixinConfig, orderDTO);
                if (payed) {
                    // update order status
                    orderService.confirmPayedWithNxLock(orderId, shopId, userId,
                            "{desc:'已完成微信支付'}");
                }
            }
        } catch (Exception e) {
        }

        return ServiceResult.success(payed);
    }

    public ServiceResult prePay(@NotNull Long shopId, @NotNull Long userId, @NotNull String appId, @NotNull Long orderId)
            throws Exception {
        // 查询shop
        ShopDO shopDO = merchantService.selectShop(shopId);
        ShopAppDO weixinConfig = merchantService.getShopAppCache(shopId, appId);

        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setShopId(shopId);
        shopMemberQuery.setId(userId);
        shopMemberQuery.setAppId(appId);
        ShopMemberDO shopMemberDO = ServiceManager.memberService.assertMem(shopMemberQuery);

        // 查询订单
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setBuyerId(userId);
        // 必须是待付款
        orderServiceQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());
        OrderDTO orderDTO = orderService.selectMainOrder(orderServiceQuery);

        // 验证库存
        itemService.verifyDeductInventory(orderServiceQuery);

        // weixin prePay
        String prepayId = weixinMiniProgramService
                .payUnifiedOrderViaWXSDK(shopDO, weixinConfig, orderDTO, shopMemberDO.getOpenId());

        // sign
        WXRequestPaymentParam wxRequestPaymentParam = weixinMiniProgramService
                .genWxRequestPaymentParam(weixinConfig, prepayId);

        // result
        WxPrePayDTO prePayVO = new WxPrePayDTO();
        BeanUtils.copyProperties(wxRequestPaymentParam, prePayVO);
        prePayVO.setOrderId(orderId.toString());
        prePayVO.setTotal(orderDTO.getRealFee().toString());
        prePayVO.setShopId(shopId.toString());
        prePayVO.setShopName(shopDO.getName());
        prePayVO.setTId(shopMemberDO.getThirdId());

        return ServiceResult.success(prePayVO);
    }
}
