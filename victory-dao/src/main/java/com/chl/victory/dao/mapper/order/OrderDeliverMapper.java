package com.chl.victory.dao.mapper.order;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.order.OrderDeliverDO;

public interface OrderDeliverMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(OrderDeliverDO record);

    //int insertSelective(OrderDeliverDO record);

    /*OrderDeliverDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderDeliverDO record);*/

    //int updateByPrimaryKey(OrderDeliverDO record);
}