package com.chl.victory.dao.mapper.order;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.order.PayOrderDO;

public interface PayOrderMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(PayOrderDO record);

    //int insertSelective(PayOrderDO record);

    /*PayOrderDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PayOrderDO record);*/

    //int updateByPrimaryKey(PayOrderDO record);
}