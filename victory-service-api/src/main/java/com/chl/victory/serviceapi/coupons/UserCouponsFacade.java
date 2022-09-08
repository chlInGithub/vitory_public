package com.chl.victory.serviceapi.coupons;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.coupons.model.UserCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.UserCouponsQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:54
 **/
public interface UserCouponsFacade {

    ServiceResult<List<UserCouponsDTO>> select(UserCouponsQueryDTO userCouponsQuery);

    ServiceResult<Integer> count(UserCouponsQueryDTO userCoupons);

    ServiceResult gain(UserCouponsDTO userCouponsDTO);
}
