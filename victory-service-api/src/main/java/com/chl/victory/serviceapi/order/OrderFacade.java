package com.chl.victory.serviceapi.order;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.serviceapi.ServiceResult;
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

/**
 * @author ChenHailong
 * @date 2020/8/24 17:00
 **/
public interface OrderFacade {

    String getSaleAndOrderSummary(@NotNull Long shopId);

    ServiceResult<List<OrderDTO>> selectMains(OrderQueryDTO query);

    ServiceResult updateOrderNote(@NotNull @Positive Long orderId, @NotEmpty String note,
            @NotNull @Positive Long shopId);

    ServiceResult sent(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotEmpty String logisticsCP, @NotEmpty String logisticsNo);

    ServiceResult delOrder(List<Long> mainIds, Long shopId);

    ServiceResult okRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note, @NotNull Long shopId,
            @NotNull Long operatorId);

    ServiceResult refuseRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId);

    ServiceResult updateOrder(Long orderId, Long shopId, Long operatorId, OrderStatusEnum orderStatusEnum,
            List<OrderStatusEnum> currentOrderStatusEnums);

    ServiceResult<Integer> countPayOder(PayOrderQueryDTO payQuery);

    ServiceResult confirmPayedWithNxLock(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotNull String context);

    ServiceResult<Integer> countDeliverOrder(OrderDeliverQueryDTO deliverQuery);

    ServiceResult deliveryCompleted(Long orderId, Long shopId, Long operatorId, String context);

    ServiceResult<Integer> countOrder(OrderQueryDTO query);

    ServiceResult<List<OrderStatusStatisDTO>> countStatus(@NotNull OrderQueryDTO orderServiceQuery);

    ServiceResult<OrderDTO> selectMainOrder(OrderQueryDTO query);

    ServiceResult delOrderAndCach2C(Long shopId, Long userId, Long orderId);

    ServiceResult<Long> createOrderWithNxLock(OrderCreateDTO orderCreateDTO) throws Exception;

    ServiceResult<Long> createRefundWithNxLock(RefundDTO refundDTO);

    ServiceResult<SettleDTO> computeForSettle(OrderCreateDTO orderCreateDTO);

    void closeTimeoutNotPayedOrder4OnlinePay(int minutes);

    void closeTimeoutNotPayedOrder4OfflinePay(int minutes);

    List<Long> selectRefundIds(RefundOrderQueryDTO refundOrderQuery);

    ServiceResult applyFor3rdPlatFormRefundWithNxLock(Long refundId);

    ServiceResult dealRefundedResult(Long refundId);

    void dealPresellAfterEnd();
}
