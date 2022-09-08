package com.chl.victory.dao.mapper.sms;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.sms.SmsSetHistoryDO;

public interface SmsSetHistoryMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SmsSetHistoryDO record);

    /*int insertSelective(SmsSetHistoryDO record);

    SmsSetHistoryDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SmsSetHistoryDO record);

    int updateByPrimaryKey(SmsSetHistoryDO record);*/
}