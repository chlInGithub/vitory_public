package com.chl.victory.serviceimpl.test.dao.activity;

import com.chl.victory.dao.manager.activity.ShopActivityManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.activity.ShopActivityDO;
import com.chl.victory.dao.query.activity.ShopActivityQuery;
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
public class ShopActivityManagerTest extends BaseTest {
    @Resource
    ShopActivityManager shopActivityManager;

    static Long id;

    @Test
    public void test1_verifyBean(){
        Assert.assertNotNull(shopActivityManager);
    }

    @Test
    public void test2_save() {
        ShopActivityDO model = new ShopActivityDO();
        model.setShopId(1120190521153045430L);
        model.setOperatorId(1L);
        model.setValidTime(new Date());
        model.setInvalidTime(new Date());
        model.setTitle("xxx店铺活动xx");
        model.setDesc("descxxxxx");
        model.setMeet(new BigDecimal("79.01").setScale(2));
        model.setDiscount(new BigDecimal("10.01").setScale(2));
        model.setOnly(true);
        model.setRepeat(false);
        model.setOrder(10);
        model.setStatus(true);
        try {
            int row = shopActivityManager.save(model);
            Assert.assertEquals(1, row);
            id = model.getId();
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3_select() {
        ShopActivityQuery query = new ShopActivityQuery();
        query.setShopId(1L);
        try {
            List<ShopActivityDO> models = shopActivityManager.select(query);
            Assert.assertNotNull(models);
            Assert.assertTrue(models.size() > 0);

            query.setId(id);
            models = shopActivityManager.select(query);
            System.out.println(models);
            Assert.assertNotNull(models);
            Assert.assertTrue(models.size() == 1);
            Assert.assertEquals(id, models.get(0).getId());
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4_del() {
        ShopActivityQuery query = new ShopActivityQuery();
        query.setShopId(1L);
        try {
            int row = shopActivityManager.del(query);
            Assert.assertEquals(0, row);

            query.setId(id);
            row = shopActivityManager.del(query);
            Assert.assertEquals(1, row);
        } catch (DaoManagerException e) {
            e.printStackTrace();
        }
    }
}
