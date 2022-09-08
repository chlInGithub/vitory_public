package com.chl.victory.service.services.sms;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.sms.SmsManager;
import com.chl.victory.dao.model.sms.SmsHistoryDO;
import com.chl.victory.dao.model.sms.SmsSendingDO;
import com.chl.victory.dao.query.sms.SmsSendingQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信发送业务服务接口
 * @author ChenHailong
 * @date 2019/5/9 9:58
 **/
@Service
public class SmsSendingService extends BaseService {
    @Resource
    SmsManager smsManager;

    public ServiceResult saveSmsSending(SmsSendingDO smsSendingDO){
        boolean isInsert = smsSendingDO.getId() == null;
        try {
            if (!isInsert){
                SmsSendingQuery selectQuery = new SmsSendingQuery();
                selectQuery.setId(smsSendingDO.getId());
                SmsSendingDO hisSmsSendingDO = smsManager.selectSmsSending(selectQuery);
                if (null == hisSmsSendingDO){
                    return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"不存在待更新记录");
                }
                SmsHistoryDO historyDO = new SmsHistoryDO();
                BeanUtils.copyProperties(hisSmsSendingDO, historyDO);
                historyDO.setSmsSendingId(hisSmsSendingDO.getId());
                historyDO.setId(null);
                smsManager.saveSmsHistory(historyDO);
            }
            smsManager.saveSmsSending(smsSendingDO);
            return ServiceResult.success();
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<SmsSendingDO>> selectSmsSending(SmsSendingQuery query){
        try {
            return ServiceResult.success(smsManager.selectSmsSending(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
}
