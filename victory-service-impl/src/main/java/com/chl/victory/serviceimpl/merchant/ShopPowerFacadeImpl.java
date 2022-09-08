package com.chl.victory.serviceimpl.merchant;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.merchant.ShopPowerFacade;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author ChenHailong
 * @date 2020/9/2 15:58
 **/
@DubboService
public class ShopPowerFacadeImpl implements ShopPowerFacade {

    @Override
    public boolean hasValidWeiSales(Long shopId) {
        return ServiceManager.shopPowerService.hasValidWeiSales(shopId);
    }
}
