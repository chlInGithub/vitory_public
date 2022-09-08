package com.chl.victory.serviceimpl.coupons;

import java.util.List;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.coupons.ShopCouponsFacade;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.shopCouponsService;

/**
 * @author ChenHailong
 * @date 2020/9/1 15:00
 **/
@DubboService
public class ShopCouponsFacadeImpl implements ShopCouponsFacade {

    @Override
    public ServiceResult<Integer> count(ShopCouponsQueryDTO query) {
        return shopCouponsService.count(query);
    }

    @Override
    public ServiceResult save(ShopCouponsDTO dto, List<Long> itemIds) {
        return shopCouponsService.save(dto, itemIds);
    }

    @Override
    public ServiceResult del(Long id, Long shopId) {
        return shopCouponsService.del(id, shopId);
    }

    @Override
    public ServiceResult<List<ShopCouponsDTO>> select(ShopCouponsQueryDTO query) {
        return shopCouponsService.select(query);
    }

    @Override
    public ServiceResult valid(Long id, Long shopId) {
        return shopCouponsService.valid(id, shopId);
    }

    @Override
    public ServiceResult invalid(Long id, Long shopId) {
        return shopCouponsService.invalid(id, shopId);
    }
}
