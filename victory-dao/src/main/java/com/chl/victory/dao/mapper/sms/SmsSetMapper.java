package com.chl.victory.dao.mapper.sms;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.sms.SmsSetDO;

public interface SmsSetMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SmsSetDO record);

    //int insertSelective(SmsSetDO record);

    /*SmsSetDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SmsSetDO record);

    int updateByPrimaryKey(SmsSetDO record);*/
}