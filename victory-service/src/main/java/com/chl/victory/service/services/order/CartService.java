package com.chl.victory.service.services.order;

import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.service.services.order.OrderService.CreatedOrderContext;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.cart.model.CartComputeDTO;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceapi.order.model.OrderFeeComputeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.common.util.ExceptionUtil.trimExMsg;
import static com.chl.victory.service.services.ServiceManager.orderService;

/**
 * 购物车服务
 * @author ChenHailong
 * @date 2019/10/28 16:55
 **/
@Validated
@Slf4j
@Service
public class CartService {

    /**
     * 用于购物车计算金额
     */
    public ServiceResult<CartComputeDTO> computeForCart(@NotNull(message = "缺少订单创建信息") OrderFeeComputeDTO computeDTO) {
        ValidationUtil.validate(computeDTO);
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        BeanUtils.copyProperties(computeDTO, orderCreateDTO);
        CreatedOrderContext context = new CreatedOrderContext();
        context.orderCreateDTO = OrderService.transfer2OrderCreateTemp(orderCreateDTO);

        try {
            context.autoChooseBestShopActivity = true;
            context.autoChooseBestShopCoupons = true;
            orderService.genOrderAndSub(context);
            orderService.computeFee(context);
        } catch (Exception e) {
            CartService.log.error("computeForCart{}", orderCreateDTO, e);
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, trimExMsg(e));
        }

        // 组装结果
        CartComputeDTO cartComputeDTO = genCartComputeDTO(context);

        return ServiceResult.success(cartComputeDTO);
    }

    private CartComputeDTO genCartComputeDTO(CreatedOrderContext context) {
        CartComputeDTO cartComputeDTO = new CartComputeDTO();
        cartComputeDTO.setItemCount(context.subOrderDOS.stream().mapToInt(SubOrderDO::getCount).sum());
        cartComputeDTO.setActivityDTO(OrderService.transfer2ActivityDTO(context.orderActivtyDO));
        cartComputeDTO.setCouponsDTO(OrderService.transfer2CouponsDTO(context.orderCouponsDO));
        cartComputeDTO.setRealFee(context.mainOrder.getRealFee());
        cartComputeDTO.setTotalFee(context.mainOrder.getTotalFee());
        return cartComputeDTO;
    }
}
