package com.chl.victory.serviceapi.coupons;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:54
 **/
public interface ShopCouponsFacade {

    ServiceResult<Integer> count(ShopCouponsQueryDTO query);

    ServiceResult save(ShopCouponsDTO model, List<Long> itemIds);

    ServiceResult del(Long id, Long shopId);

    ServiceResult<List<ShopCouponsDTO>> select(ShopCouponsQueryDTO query);

    ServiceResult valid(Long id, Long shopId);

    ServiceResult invalid(Long id, Long shopId);
}
