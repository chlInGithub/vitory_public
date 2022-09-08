package com.chl.victory.serviceimpl.test.dao.sms;

import com.chl.victory.dao.manager.sms.SmsManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.sms.SmsHistoryDO;
import com.chl.victory.dao.model.sms.SmsSendingDO;
import com.chl.victory.dao.query.sms.SmsHistoryQuery;
import com.chl.victory.dao.query.sms.SmsSendingQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;

import java.util.Date;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmsManagerTest extends BaseTest {
    @Resource
    SmsManager smsManager;
    static Long smsSendingId;
    @Test
    public void test01_saveSmsSending() throws DaoManagerException {
        SmsSendingDO model = new SmsSendingDO();
        model.setShopId(1L);
        model.setToMobile(12323432234L);
        model.setSmsTempId(121L);
        model.setValues("zzz|xxx");
        model.setStatus((byte)0);
        Assert.assertEquals(1, smsManager.saveSmsSending(model));
        smsSendingId = model.getId();
    }
    @Test
    public void test02_updateSmsSending() throws DaoManagerException {
        SmsSendingQuery query = new SmsSendingQuery();
        query.setId(smsSendingId);
        SmsSendingDO smsSendingDO = smsManager.selectSmsSending(query);
        SmsHistoryDO historyDO = new SmsHistoryDO();
        BeanUtils.copyProperties(smsSendingDO, historyDO);
        historyDO.setId(null);
        historyDO.setSmsSendingId(smsSendingDO.getId());
        Assert.assertEquals(1, smsManager.saveSmsHistory(historyDO));

        SmsSendingDO model = new SmsSendingDO();
        model.setId(smsSendingId);
        model.setStatus((byte)1);
        model.setLastTime(new Date());
        model.setResult("发送成功");
        Assert.assertEquals(1, smsManager.saveSmsSending(model));
    }

    @Test
    public void test03_selectSmsSendings() throws DaoManagerException {
        SmsSendingQuery query = new SmsSendingQuery();
        query.setShopId(1L);
        query.setToMobile(12323432234L);
        Assert.assertTrue(smsManager.selectSmsSendings(query).size() > 0);

        SmsHistoryQuery query1 = new SmsHistoryQuery();
        query1.setSmsSendingId(smsSendingId);
        query1.setShopId(1L);
        query1.setToMobile(12323432234L);
        Assert.assertTrue(smsManager.selectSmsHistorys(query1).size() > 0);
    }

    @Test
    public void test04_delSmsSending() throws DaoManagerException {
        SmsSendingQuery query = new SmsSendingQuery();
        query.setShopId(1L);
        query.setId(smsSendingId);
        query.setToMobile(12323432234L);
        Assert.assertEquals(1, smsManager.delSmsSending(query));
    }
}