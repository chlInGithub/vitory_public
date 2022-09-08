package com.chl.victory.serviceimpl.order;

import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.order.OrderFacade;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.model.OrderStatusStatisDTO;
import com.chl.victory.serviceapi.order.model.RefundDTO;
import com.chl.victory.serviceapi.order.model.SettleDTO;
import com.chl.victory.serviceapi.order.query.OrderDeliverQueryDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.order.query.PayOrderQueryDTO;
import com.chl.victory.serviceapi.order.query.RefundOrderQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.orderLocalService;
import static com.chl.victory.service.services.ServiceManager.orderService;

/**
 * @author ChenHailong
 * @date 2020/9/2 16:02
 **/
@DubboService
public class OrderFacadeImpl implements OrderFacade {

    @Override
    public String getSaleAndOrderSummary(@NotNull Long shopId) {
        return orderService.getSaleAndOrderSummary(shopId);
    }

    @Override
    public ServiceResult<List<OrderDTO>> selectMains(OrderQueryDTO query) {
        return orderService.selectMains(query);
    }

    @Override
    public ServiceResult updateOrderNote(@NotNull @Positive Long orderId, @NotEmpty String note,
            @NotNull @Positive Long shopId) {
        return orderService.updateOrderNote(orderId, note, shopId);
    }

    @Override
    public ServiceResult sent(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotEmpty String logisticsCP, @NotEmpty String logisticsNo) {
        return orderService.sent(orderId, shopId, operatorId, logisticsCP, logisticsNo);
    }

    @Override
    public ServiceResult delOrder(List<Long> mainIds, Long shopId) {
        try {
            return orderService.delOrder(mainIds, shopId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult okRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId) {
        try {
            return orderService.okRefund(orderId, refundId, note, shopId, operatorId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult refuseRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId) {
        try {
            return orderService.refuseRefund(orderId, refundId, note, shopId, operatorId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult updateOrder(Long orderId, Long shopId, Long operatorId, OrderStatusEnum orderStatusEnum,
            List<OrderStatusEnum> currentOrderStatusEnums) {
        try {
            return orderService.updateOrder(orderId, shopId, operatorId, orderStatusEnum, currentOrderStatusEnums);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult<Integer> countPayOder(PayOrderQueryDTO payQuery) {
        return orderService.countPayOder(payQuery);
    }

    @Override
    public ServiceResult confirmPayedWithNxLock(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotNull String context) {
        return orderService.confirmPayedWithNxLock(orderId, shopId, operatorId, context);
    }

    @Override
    public ServiceResult<Integer> countDeliverOrder(OrderDeliverQueryDTO deliverQuery) {
        return orderService.countDeliverOrder(deliverQuery);
    }

    @Override
    public ServiceResult deliveryCompleted(Long orderId, Long shopId, Long operatorId, String context) {
        return orderService.deliveryCompleted(orderId, shopId, operatorId, context);
    }

    @Override
    public ServiceResult<Integer> countOrder(OrderQueryDTO query) {
        return orderService.countOrder(query);
    }

    @Override
    public ServiceResult<List<OrderStatusStatisDTO>> countStatus(@NotNull OrderQueryDTO orderServiceQuery) {
        return orderService.countStatus(orderServiceQuery);
    }

    @Override
    public ServiceResult<OrderDTO> selectMainOrder(OrderQueryDTO query) {
        try {
            return ServiceResult.success(orderService.selectMainOrder(query));
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult delOrderAndCach2C(Long shopId, Long userId, Long orderId){
        try {
            orderService.delOrder(Arrays.asList(orderId), shopId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        orderLocalService.delOrderCache2C(shopId, userId, orderId);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult<Long> createOrderWithNxLock(OrderCreateDTO orderCreateDTO) throws Exception {
        return orderService.createOrderWithNxLock(orderCreateDTO);
    }

    @Override
    public ServiceResult<Long> createRefundWithNxLock(RefundDTO refundDTO) {
        try {
            return orderService.createRefundWithNxLock(refundDTO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult<SettleDTO> computeForSettle(OrderCreateDTO orderCreateDTO) {
        return orderService.computeForSettle(orderCreateDTO);
    }

    @Override
    public void closeTimeoutNotPayedOrder4OnlinePay(int minutes) {
        orderService.closeTimeoutNotPayedOrder4OnlinePay(minutes);
    }

    @Override
    public void closeTimeoutNotPayedOrder4OfflinePay(int minutes) {
        orderService.closeTimeoutNotPayedOrder4OfflinePay(minutes);
    }

    @Override
    public List<Long> selectRefundIds(RefundOrderQueryDTO refundOrderQuery) {
        return orderService.selectRefundIds(refundOrderQuery);
    }

    @Override
    public ServiceResult applyFor3rdPlatFormRefundWithNxLock(Long refundId) {
        try {
            orderService.applyFor3rdPlatFormRefundWithNxLock(refundId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult dealRefundedResult(Long refundId) {
        try {
            orderService.dealRefundedResult(refundId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public void dealPresellAfterEnd() {
        orderService.dealPresellAfterEnd();
    }
}
