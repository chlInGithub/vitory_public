package com.chl.victory.dao.mapper.sms;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.sms.SmsHistoryDO;

public interface SmsHistoryMapper  extends BaseMapper {
    /*int deleteByPrimaryKey(Long id);*/

    int insert(SmsHistoryDO record);

    //int insertSelective(SmsHistoryDO record);

    //SmsHistoryDO selectByPrimaryKey(Long id);

    //int updateByPrimaryKeySelective(SmsHistoryDO record);

    //int updateByPrimaryKey(SmsHistoryDO record);
}