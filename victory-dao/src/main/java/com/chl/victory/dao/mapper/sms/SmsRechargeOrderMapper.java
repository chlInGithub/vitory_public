package com.chl.victory.dao.mapper.sms;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.sms.SmsRechargeOrderDO;

public interface SmsRechargeOrderMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SmsRechargeOrderDO record);

    //int insertSelective(SmsRechargeOrderDO record);

    //SmsRechargeOrderDO selectByPrimaryKey(Long id);

    //int updateByPrimaryKeySelective(SmsRechargeOrderDO record);

    //int updateByPrimaryKey(SmsRechargeOrderDO record);
}