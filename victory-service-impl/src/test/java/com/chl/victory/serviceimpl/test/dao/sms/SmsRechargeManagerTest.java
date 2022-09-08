package com.chl.victory.serviceimpl.test.dao.sms;

import com.chl.victory.dao.manager.sms.SmsRechargeManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.sms.SmsRechargeOrderDO;
import com.chl.victory.dao.query.sms.SmsRechargeOrderQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmsRechargeManagerTest extends BaseTest {
    @Resource
    SmsRechargeManager smsRechargeManager;
    static Long id;

    @Test
    public void test01_saveSmsRecharge() throws DaoManagerException {
        SmsRechargeOrderDO model = new SmsRechargeOrderDO();
        model.setOrderId(1L);
        model.setStatus((byte)10);
        model.setShopId(10001L);
        model.setOperatorId(1110101001L);
        Assert.assertEquals(1, smsRechargeManager.saveSmsRecharge(model));
        id = model.getId();
    }

    @Test
    public void test02_updateSmsRecharges() throws DaoManagerException {
        SmsRechargeOrderDO model = new SmsRechargeOrderDO();
        model.setId(id);
        model.setOrderId(1L);
        model.setStatus((byte)20);
        model.setShopId(10001L);
        model.setOperatorId(1110101001L);
        Assert.assertEquals(1, smsRechargeManager.saveSmsRecharge(model));
    }
    @Test
    public void test03_selectSmsRecharges() throws DaoManagerException {
        SmsRechargeOrderQuery query = new SmsRechargeOrderQuery();
        query.setOrderId(1L);
        Assert.assertTrue(smsRechargeManager.selectSmsRecharges(query).size() > 0);
    }

    @Test
    public void test04_delSmsRecharge() throws DaoManagerException {
        SmsRechargeOrderQuery query = new SmsRechargeOrderQuery();
        query.setOrderId(1L);
        query.setId(id);
        Assert.assertEquals(1, smsRechargeManager.delSmsRecharge(query));
    }
}