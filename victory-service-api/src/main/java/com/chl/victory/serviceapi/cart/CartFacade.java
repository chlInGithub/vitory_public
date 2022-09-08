package com.chl.victory.serviceapi.cart;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.cart.model.CartComputeDTO;
import com.chl.victory.serviceapi.order.model.OrderFeeComputeDTO;

/**
 * @author ChenHailong
 * @date 2020/8/25 17:11
 **/
public interface CartFacade {

    ServiceResult<CartComputeDTO> computeForCart(OrderFeeComputeDTO computeDTO);
}
