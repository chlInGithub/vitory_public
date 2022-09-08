package com.chl.victory.dao.mapper.order;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.order.OrderQuery;
import com.chl.victory.dao.query.order.SubOrderQuery;

public interface SubOrderMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SubOrderDO record);

    Integer countItem(SubOrderQuery orderQuery);

    //int insertSelective(SubOrderDO record);

    /*SubOrderDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SubOrderDO record);*/

    //int updateByPrimaryKey(SubOrderDO record);
}