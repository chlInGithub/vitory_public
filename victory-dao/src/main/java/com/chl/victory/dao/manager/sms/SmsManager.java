package com.chl.victory.dao.manager.sms;

import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.mapper.sms.SmsHistoryMapper;
import com.chl.victory.dao.mapper.sms.SmsSendingMapper;
import com.chl.victory.dao.model.sms.SmsHistoryDO;
import com.chl.victory.dao.model.sms.SmsSendingDO;
import com.chl.victory.dao.query.sms.SmsHistoryQuery;
import com.chl.victory.dao.query.sms.SmsSendingQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发送短信数据层访问入口
 * @author hailongchen9
 */
@Component
public class SmsManager extends BaseManager4Mybatis {
    @Resource
    SmsSendingMapper smsSendingMapper;
    @Resource
    SmsHistoryMapper smsHistoryMapper;

    public int saveSmsSending(SmsSendingDO model) throws DaoManagerException {
        SmsSendingQuery checkOnlyOne = null;
        SmsSendingQuery checkNotExist = null;
        if (model.getId() == null){
            if (model.getShopId() == null
                    || model.getToMobile() == null
                    || model.getSmsTempId() == null
                    || model.getStatus() == null){
                throw new DaoManagerException("缺少数据，如短信模板ID、归属店铺ID、短信接收者等");
            }
            checkNotExist = new SmsSendingQuery();
            checkNotExist.setShopId(model.getShopId());
            checkNotExist.setToMobile(model.getToMobile());
            checkNotExist.setSmsTempId(model.getSmsTempId());
            checkNotExist.setValues(model.getValues());
            checkNotExist.setStatus(model.getStatus());
        }else {
            checkOnlyOne = new SmsSendingQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setToMobile(model.getToMobile());
            checkOnlyOne.setSmsTempId(model.getSmsTempId());
        }
        return save(smsSendingMapper, model, checkNotExist, checkOnlyOne, TableNameEnum.SMS_SENDING);
    }

    public List<SmsSendingDO> selectSmsSendings(SmsSendingQuery query) throws DaoManagerException {
        return select(smsSendingMapper, query);
    }
    public SmsSendingDO selectSmsSending(SmsSendingQuery query) throws DaoManagerException {
        return selectOne(smsSendingMapper, query);
    }
    public int delSmsSending(SmsSendingQuery query) throws DaoManagerException {
        return del(smsSendingMapper, query);
    }

    /**
     * 仅仅插入历史数据，不允许修改
     * @param model
     * @return
     * @throws DaoManagerException
     */
    public int saveSmsHistory(SmsHistoryDO model) throws DaoManagerException {
        if (model.getId() == null){
            if (model.getShopId() == null
                    || model.getToMobile() == null
                    || model.getSmsTempId() == null
                    || model.getStatus() == null
                    || model.getSmsSendingId() == null){
                throw new DaoManagerException("缺少数据，如短信模板ID、归属店铺ID、短信接收者等");
            }
            return save(smsHistoryMapper, model, null, null, TableNameEnum.SMS_HISTORY);
        }
        return 0;
    }

    public List<SmsHistoryDO> selectSmsHistorys(SmsHistoryQuery query) throws DaoManagerException {
        return select(smsHistoryMapper, query);
    }
}
