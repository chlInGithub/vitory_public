package com.chl.victory.dao.mapper.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.order.OrderDO;
import com.chl.victory.dao.query.order.OrderQuery;

public interface OrderMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(OrderDO record);

    BigDecimal saleTotal(OrderQuery orderQuery);

    Integer countMem(OrderQuery orderQuery);

    Integer closeTimeoutNotPayedOrder4OfflinePay(Date date);
    Integer closeTimeoutNotPayedOrder4OnlinePay(Date date);
    List<OrderDO> selectNeedCloseTimeoutNotPayedOrder4OfflinePay(Date date);
    List<OrderDO> selectNeedCloseTimeoutNotPayedOrder4OnlinePay(Date date);

    //int insertSelective(OrderDO record);

    /*OrderDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderDO record);*/

    //int updateByPrimaryKey(OrderDO record);
}