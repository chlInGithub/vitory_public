package com.chl.victory.service.services.merchant;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.dao.manager.merchant.ShopPowerManager;
import com.chl.victory.serviceapi.merchant.enums.ShopPowerEnum;
import org.springframework.stereotype.Service;

/**
 * @author ChenHailong
 * @date 2020/8/19 17:12
 **/
@Service
public class ShopPowerService {
    @Resource
    ShopPowerManager shopPowerManager;

    /**
     * 是否具有某个合法的增值能力
     * @param shopId
     * @param type {@link com.chl.victory.serviceapi.merchant.enums.ShopPowerEnum}
     * @return
     */
    public boolean hasValid(@NotNull Long shopId, @NotNull Byte type){
        return shopPowerManager.valid(shopId, type);
    }

    /**
     * 微导购能力？
     * @param shopId
     * @return
     */
    public boolean hasValidWeiSales(@NotNull Long shopId) {
        return hasValid(shopId, ShopPowerEnum.weiSales.getCode());
    }
}
