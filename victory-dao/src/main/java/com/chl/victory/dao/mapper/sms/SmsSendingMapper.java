package com.chl.victory.dao.mapper.sms;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.sms.SmsSendingDO;

public interface SmsSendingMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SmsSendingDO record);

    //int insertSelective(SmsSendingDO record);

    //SmsSendingDO selectByPrimaryKey(Long id);

    //int updateByPrimaryKeySelective(SmsSendingDO record);

    //int updateByPrimaryKey(SmsSendingDO record);
}