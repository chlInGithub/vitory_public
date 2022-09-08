package com.chl.victory.serviceimpl.cart;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.cart.CartFacade;
import com.chl.victory.serviceapi.cart.model.CartComputeDTO;
import com.chl.victory.serviceapi.order.model.OrderFeeComputeDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.cartService;

/**
 * @author ChenHailong
 * @date 2020/9/1 14:45
 **/
@DubboService
public class CartFacadeImpl implements CartFacade {

    @Override
    public ServiceResult<CartComputeDTO> computeForCart(OrderFeeComputeDTO computeDTO) {
        return cartService.computeForCart(computeDTO);
    }
}
