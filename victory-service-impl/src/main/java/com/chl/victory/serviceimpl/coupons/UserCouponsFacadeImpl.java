package com.chl.victory.serviceimpl.coupons;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.coupons.UserCouponsFacade;
import com.chl.victory.serviceapi.coupons.model.UserCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.UserCouponsQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.userCouponsService;

/**
 * @author ChenHailong
 * @date 2020/9/1 16:52
 **/
@DubboService
public class UserCouponsFacadeImpl implements UserCouponsFacade {

    @Override
    public ServiceResult<List<UserCouponsDTO>> select(UserCouponsQueryDTO userCouponsQuery) {
        return userCouponsService.select(userCouponsQuery);
    }

    @Override
    public ServiceResult<Integer> count(UserCouponsQueryDTO userCoupons) {
        return userCouponsService.count(userCoupons);
    }

    @Override
    public ServiceResult gain(UserCouponsDTO userCouponsDTO) {
        return userCouponsService.gain(userCouponsDTO);
    }
}
