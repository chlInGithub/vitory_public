package com.chl.victory.dao.manager.sms;

import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.mapper.sms.SmsSetMapper;
import com.chl.victory.dao.model.sms.SmsSetDO;
import com.chl.victory.dao.query.sms.SmsSetQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信套餐数据层访问入口
 * @author hailongchen9
 */
@Component
public class SmsSetManager extends BaseManager4Mybatis {
    @Resource
    SmsSetMapper smsSetMapper;
    
    public int saveSmsSet(SmsSetDO model) throws DaoManagerException {
        SmsSetQuery checkOnlyOne = null;
        SmsSetQuery checkNotExist = null;
        if (model.getId() == null){
            if (model.getItemId() == null
                    || model.getOperatorId() == null
                    || model.getNum() == null){
                throw new DaoManagerException("缺少数据，如关联商品ID、操作者ID、短信数量等");
            }
        }else {
            checkOnlyOne = new SmsSetQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setItemId(model.getItemId());
        }
        return save(smsSetMapper, model, checkNotExist, checkOnlyOne, TableNameEnum.SMS_SET);
    }

    public List<SmsSetDO> selectSmsSets(SmsSetQuery query) throws DaoManagerException {
        return select(smsSetMapper, query);
    }
    public SmsSetDO selectSmsSet(SmsSetQuery query) throws DaoManagerException {
        return selectOne(smsSetMapper, query);
    }
    public int delSmsSet(SmsSetQuery query) throws DaoManagerException {
        return del(smsSetMapper, query);
    }
}
