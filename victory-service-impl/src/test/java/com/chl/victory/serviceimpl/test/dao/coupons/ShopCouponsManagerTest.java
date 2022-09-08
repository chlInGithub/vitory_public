package com.chl.victory.serviceimpl.test.dao.coupons;

import com.chl.victory.dao.manager.coupons.ShopCouponsManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;
import com.chl.victory.dao.query.coupons.ShopCouponsQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShopCouponsManagerTest extends BaseTest {
    @Resource
    ShopCouponsManager shopCouponsManager;
    static Long id;
    @Test
    public void test01_save() throws DaoManagerException {
        ShopCouponsDO shopCouponsDO = new ShopCouponsDO();
        shopCouponsDO.setDesc("描述");
        shopCouponsDO.setDiscount(new BigDecimal(5));
        shopCouponsDO.setInvalidTime(new Date());
        shopCouponsDO.setMeet(new BigDecimal(50));
        shopCouponsDO.setOnly(true);
        shopCouponsDO.setPriority((byte)1);
        shopCouponsDO.setStatus(true);
        shopCouponsDO.setTitle("标题");
        shopCouponsDO.setValidTime(new Date());
        shopCouponsDO.setOperatorId(1L);
        shopCouponsDO.setShopId(1L);
        int result = shopCouponsManager.save(shopCouponsDO);
        Assert.assertEquals(1, result);
        id = shopCouponsDO.getId();
    }

    @Test
    public void test02_select() throws DaoManagerException {
        ShopCouponsDO shopCouponsDO = new ShopCouponsDO();
        shopCouponsDO.setId(id);
        shopCouponsDO.setDesc("描述modi");
        shopCouponsDO.setDiscount(new BigDecimal("5.5"));
        shopCouponsDO.setInvalidTime(new Date());
        shopCouponsDO.setMeet(new BigDecimal(49));
        shopCouponsDO.setOnly(false);
        shopCouponsDO.setPriority((byte)3);
        shopCouponsDO.setStatus(true);
        shopCouponsDO.setTitle("标题modi");
        shopCouponsDO.setValidTime(new Date());
        shopCouponsDO.setOperatorId(1L);
        shopCouponsDO.setShopId(1L);
        int result = shopCouponsManager.save(shopCouponsDO);
        Assert.assertEquals(1, result);


        ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
        shopCouponsQuery.setShopId(1L);
        List<ShopCouponsDO> shopCouponsDOS = shopCouponsManager.select(shopCouponsQuery);
        Assert.assertNotNull(shopCouponsDOS);
        Assert.assertTrue(shopCouponsDOS.size() > 0);
    }

    @Test
    public void test03_del() throws DaoManagerException {
        ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
        shopCouponsQuery.setShopId(1L);
        shopCouponsQuery.setId(id);
        int result = shopCouponsManager.del(shopCouponsQuery);
        Assert.assertEquals(1, result);
    }
}