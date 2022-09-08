package com.chl.victory.serviceimpl.test.dao.sms;

import com.chl.victory.dao.manager.sms.SmsSetManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.sms.SmsSetDO;
import com.chl.victory.dao.query.sms.SmsSetQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;
import java.util.Date;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmsSetManagerTest extends BaseTest {
    @Resource
    SmsSetManager smsSetManager;
    static Long id;
    @Test
    public void test01_saveSmsSet() throws DaoManagerException {
        SmsSetDO model = new SmsSetDO();
        model.setInvalidTime(new Date());
        model.setItemId(1L);
        model.setNum(100);
        model.setStatus(true);
        model.setValidTime(new Date());
        model.setOperatorId(1L);
        Assert.assertEquals(1, smsSetManager.saveSmsSet(model));
        id = model.getId();
    }
    @Test
    public void test02_updateSmsSet() throws DaoManagerException {
        SmsSetDO model = new SmsSetDO();
        model.setId(id);
        model.setItemId(1L);
        model.setNum(1001);
        model.setStatus(false);
        model.setOperatorId(1L);
        Assert.assertEquals(1, smsSetManager.saveSmsSet(model));
    }

    @Test
    public void test03_selectSmsSets() throws DaoManagerException {
        SmsSetQuery query = new SmsSetQuery();
        query.setItemId(1L);
        Assert.assertTrue(smsSetManager.selectSmsSets(query).size() > 0);
    }

    @Test
    public void test04_delSmsSet() throws DaoManagerException {
        SmsSetQuery query = new SmsSetQuery();
        query.setItemId(1L);
        query.setId(id);
        Assert.assertEquals(1, smsSetManager.delSmsSet(query));
    }
}