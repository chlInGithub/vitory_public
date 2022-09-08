package com.chl.victory.serviceimpl.test.services.coupons;


import javax.annotation.Resource;

import com.chl.victory.dao.model.coupons.UserCouponsDO;
import com.chl.victory.service.services.coupons.UserCouponsService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class UserCouponsServiceTest extends BaseTest {
    @Resource
    UserCouponsService userCouponsService;

    @Test
    public void save() {
        UserCouponsDO userCouponsDo = new UserCouponsDO();
        userCouponsDo.setUserId(4520191010134645798L);
        userCouponsDo.setCouponsId(4720191016175127927L);
        userCouponsDo.setStatus(false);
        userCouponsDo.setShopId(1120190521153045430L);
        userCouponsDo.setOperatorId(userCouponsDo.getUserId());
        ServiceResult result = userCouponsService.save(userCouponsDo);
        Assert.assertTrue(result.getSuccess());
    }

    @Test
    public void select() {
    }
}