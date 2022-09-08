package com.chl.victory.wmall.controller.shopman;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.enums.PayOrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.query.OrderDeliverQueryDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.order.query.PayOrderQueryDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.controller.order.OrderController;
import com.chl.victory.wmall.model.PageResult;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.localservice.manager.LocalServiceManager.orderLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;
import static com.chl.victory.wmall.controller.order.OrderController.transfer2OrderVO;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/shopman/order/")
public class OrderManController {


    /**
     * 订单详情
     * @return 主订单ID
     */
    @GetMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "订单详情", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result detail(@NotNull @RequestParam("id") Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId4ShopMan);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setNeedDeliverInfo(true);
        orderServiceQuery.setNeedPayOrder(true);
        orderServiceQuery.setNeedCoupons(true);
        orderServiceQuery.setNeedSubOrder(true);
        orderServiceQuery.setNeedRefund(true);
        ServiceResult<OrderDTO> orderDTOServiceResult = orderFacade.selectMainOrder(orderServiceQuery);
        if (!orderDTOServiceResult.getSuccess()) {
            return Result.FAIL(orderDTOServiceResult.getMsg());
        }
        OrderDTO orderDTO = orderDTOServiceResult.getData();

        OrderController.MainOrderVO mainOrderVO = transfer2OrderVO(orderDTO);

        return Result.SUCCESS(mainOrderVO);
    }

    /**
     * 订单列表
     * @param type 1:近1小时的50单  2:近一分钟
     * @return 主订单ID
     */
    @GetMapping(value = "getLastOrders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "订单列表", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getLastOrders(@RequestParam("type") Integer type) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        Integer pageIndex = 0;
        Integer pageSize = 50;

        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setStartedModifiedTime(DateUtils.addMinutes(new Date(), type.equals(1) ? -60 : -1));
        orderServiceQuery.setShopId(shopId4ShopMan);
        orderServiceQuery.setNeedSubOrder(true);
        orderServiceQuery.setNeedPayOrder(true);
        orderServiceQuery.setPageIndex(pageIndex);
        orderServiceQuery.setPageSize(pageSize);
        orderServiceQuery.setOrderColumn("modified_time");

        ServiceResult<List<OrderDTO>> orderDTOResult = orderFacade.selectMains(orderServiceQuery);
        if (!orderDTOResult.getSuccess()) {
            if (ServiceFailTypeEnum.NOT_EXIST.equals(orderDTOResult.getFailType())) {
                return Result.SUCCESS();
            }
            return Result.FAIL("查询订单失败");
        }

        List<OrderDTO> orderDTOs = orderDTOResult.getData();

        List<OrderController.MainOrderVO> mainOrderVOs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderDTOs)) {
            mainOrderVOs = orderDTOs.stream().map(OrderController::transfer2OrderVO).collect(Collectors.toList());
        }

        return Result.SUCCESS(mainOrderVOs);
    }


    /**
     * 确认已经自提
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "taken", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result taken(@RequestParam("orderId") Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        OrderDeliverQueryDTO deliverQuery = new OrderDeliverQueryDTO();
        deliverQuery.setOrderId(orderId);
        deliverQuery.setShopId(shopId4ShopMan);
        deliverQuery.setType(DeliverTypeEnum.noLogistics.getCode().byteValue());
        ServiceResult<Integer> countOrder = orderFacade.countDeliverOrder(deliverQuery);
        if (!countOrder.getSuccess()) {
            return Result.FAIL(countOrder.getMsg());
        }

        if (countOrder.getData() == null || countOrder.getData() < 1) {
            return Result.FAIL("订单不存在或非自提订单");
        }

        ServiceResult serviceResult = orderFacade
                .deliveryCompleted(orderId, shopId4ShopMan, userId4ShopMan, "{desc:'用户已自提商品'}");
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * 确认可以自提
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "cantake", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result cantake(@RequestParam("orderId") Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        OrderDeliverQueryDTO deliverQuery = new OrderDeliverQueryDTO();
        deliverQuery.setOrderId(orderId);
        deliverQuery.setShopId(shopId4ShopMan);
        deliverQuery.setType(DeliverTypeEnum.noLogistics.getCode().byteValue());
        ServiceResult<Integer> countOrder = orderFacade.countDeliverOrder(deliverQuery);
        if (!countOrder.getSuccess()) {
            return Result.FAIL(countOrder.getMsg());
        }

        if (countOrder.getData() == null || countOrder.getData() < 1) {
            return Result.FAIL("订单不存在或非自提订单");
        }

        ServiceResult serviceResult = orderFacade.sent(orderId, shopId4ShopMan, userId4ShopMan, "ZITI", "0");
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * 确认已线下支付
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "offlinePayed", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result offlinePayed(@RequestParam("orderId") Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        PayOrderQueryDTO payQuery = new PayOrderQueryDTO();
        payQuery.setOrderId(orderId);
        payQuery.setShopId(shopId4ShopMan);
        payQuery.setType(PayTypeEnum.offline.getCode().byteValue());
        payQuery.setStatus(PayOrderStatusEnum.newed.getVal());
        ServiceResult<Integer> countPayOrder = orderFacade.countPayOder(payQuery);
        if (!countPayOrder.getSuccess()) {
            return Result.FAIL(countPayOrder.getMsg());
        }

        if (countPayOrder.getData() == null || countPayOrder.getData() < 1) {
            return Result.FAIL("订单不存在或支付类型不符合要求");
        }

        ServiceResult serviceResult = orderFacade
                .confirmPayedWithNxLock(orderId, shopId4ShopMan, userId4ShopMan, "{desc:'商家确认'}");
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "okRefund", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result okRefund(@RequestParam("orderId") Long orderId, @RequestParam("refundId") Long refundId,
            @RequestParam("note") String note) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        ServiceResult serviceResult = orderFacade.okRefund(orderId, refundId, note, shopId4ShopMan, userId4ShopMan);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "refuseRefund", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result refuseRefund(@RequestParam("orderId") Long orderId, @RequestParam("refundId") Long refundId,
            @RequestParam("note") String note) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        ServiceResult serviceResult = orderFacade.refuseRefund(orderId, refundId, note, shopId4ShopMan, userId4ShopMan);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * 发货
     * @param orderId
     * @return
     */
    @PostMapping(path = "sent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result sent(@RequestParam("orderId") Long orderId, @RequestParam("logisticsCP") String logisticsCP,
            @RequestParam("logisticsNo") String logisticsNo) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId4ShopMan = sessionCache.getShopId4ShopMan();
        Long userId4ShopMan = sessionCache.getUserId4ShopMan();

        ServiceResult serviceResult = orderFacade
                .sent(orderId, shopId4ShopMan, userId4ShopMan, logisticsCP, logisticsNo);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }
}
