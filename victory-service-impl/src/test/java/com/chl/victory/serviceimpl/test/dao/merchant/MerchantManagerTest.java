package com.chl.victory.serviceimpl.test.dao.merchant;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.merchant.MerchantManager;
import com.chl.victory.dao.model.merchant.MerchantUserDO;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.query.merchant.MerchantUserQuery;
import com.chl.victory.dao.query.merchant.ShopQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MerchantManagerTest extends BaseTest {
    @Resource
    MerchantManager merchantManager;
    static Long shopId;
    static Long userId = 1L;

    @Test
    public void test01_saveUser() throws DaoManagerException {
        MerchantUserDO merchantUserDO = new MerchantUserDO();
        merchantUserDO.setMobile(18500425785L);
        merchantUserDO.setPass("O9rsL3jM762XOGuLCnC32ebmRw9EIebUEjWIMOnr+EIv8rYC9bVYaLQY7veoH3SXRtQIAHRk//KPEorKn8cXT62t8Oz5qSrXr6YXfG9XyAC32ckn5k7b4QlcQ2hXkQwqy6LPA64g5AOzFYUZ9TC4RglhlsKjmRHlFqTHP3GUgNE=");
        merchantUserDO.setInitPassModified(true);
        merchantUserDO.setValid(true);
        merchantUserDO.setDesc("test user");
        int result = merchantManager.saveUser(merchantUserDO);
        Assert.assertEquals(1, result);
        userId = merchantUserDO.getId();
    }

    @Test
    public void test02_saveUser() throws DaoManagerException {
        MerchantUserDO merchantUserDO = new MerchantUserDO();
        merchantUserDO.setId(userId);
        merchantUserDO.setMobile(12312341234L);
        merchantUserDO.setPass("fsfsfsfsssssfsf密文");
        merchantUserDO.setInitPassModified(true);
        int result = merchantManager.saveUser(merchantUserDO);
        Assert.assertEquals(1, result);
        Assert.assertEquals(userId, merchantUserDO.getId());
    }

    @Test
    public void test03_selectUser() throws DaoManagerException {
        List<MerchantUserDO> merchantUserDOS = merchantManager.selectUsers(new MerchantUserQuery());
        Assert.assertNotNull(merchantUserDOS);
        Assert.assertTrue(merchantUserDOS.size() > 0);
    }



    @Test
    public void test04_saveShop() {
        ShopDO model = new ShopDO();
        model.setId(1L);
        model.setOperatorId(1L);
        model.setName("店铺名称1");
        model.setMobile(12323412345L);
        ShopAppDO weixinConfig = new ShopAppDO();
        weixinConfig.setAppId("wx945e240926afff8a");
        weixinConfig.setAppSecret("0c79a5cf75274f32782203d52f2896c3");
        try {
            int result = merchantManager.saveShop(model);
            Assert.assertEquals(1, result);
            shopId = model.getId();
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test05_saveShop(){
        ShopDO model = new ShopDO();
        model.setId(shopId);
        model.setName("店铺名称1更新");
        model.setMobile(12345454545L);
        try {
            int result = merchantManager.saveShop(model);
            Assert.assertEquals(1, result);
            Assert.assertEquals(shopId, model.getId());
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test06_selectShop(){
        ShopQuery query = new ShopQuery();
        query.setId(shopId);
        try {
            List<ShopDO> shopDOS = merchantManager.selectShops(query, userId);
            Assert.assertNotNull(shopDOS);
            Assert.assertTrue(shopDOS.size() > 0);
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test07_delShop(){
        ShopQuery query = new ShopQuery();
        query.setId(shopId);
        query.setMobile(12345454545L);
        try {
            int result = merchantManager.delShop(query);
            Assert.assertEquals(1, result);
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test08_delUser(){
        MerchantUserQuery query = new MerchantUserQuery();
        query.setId(userId);
        query.setMobile(12312341234L);
        try {
            int result = merchantManager.delUser(query);
            Assert.assertEquals(1, result);
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

}