package com.chl.victory.service.services.sms;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.sms.SmsSetManager;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.model.sms.SmsSetDO;
import com.chl.victory.dao.query.sms.SmsSetQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.service.services.item.ItemService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 短信充值套餐业务服务入口
 * @author ChenHailong
 * @date 2019/5/8 16:42
 **/
@Service
public class SmsSetService extends BaseService {
    @Resource
    SmsSetManager smsSetManager;
    @Resource
    ItemService itemService;

    public ServiceResult saveSet(ItemDO itemDO, SmsSetDO smsSetDO){
        ServiceResult serviceResult = itemService.save(itemDO, null);
        if (!serviceResult.getSuccess()){
            return serviceResult;
        }

        smsSetDO.setItemId(itemDO.getId());
        try {
            smsSetManager.saveSmsSet(smsSetDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        return ServiceResult.success();
    }

    public ServiceResult<List<SmsSetDO>> selectSmsSets(SmsSetQuery query){
        try {
            return ServiceResult.success(smsSetManager.selectSmsSets(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
    public ServiceResult<SmsSetDO> selectSmsSet(Long smsSetId){
        SmsSetQuery query = new SmsSetQuery();
        query.setId(smsSetId);
        try {
            return ServiceResult.success(smsSetManager.selectSmsSets(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult delSmsSet(Long smsSetId){
        SmsSetQuery query = new SmsSetQuery();
        query.setId(smsSetId);
        try {
            SmsSetDO smsSetDO = smsSetManager.selectSmsSet(query);
            if (null == smsSetDO){
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "记录不存在");
            }
            int affectRow = smsSetManager.delSmsSet(query);
            if (1 == affectRow){
                List<Long> itemids = new ArrayList<>();
                itemids.add(smsSetDO.getItemId());
                return itemService.delItem(null, itemids);
            }
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
}
