package com.chl.victory.dao.mapper.order;

import java.util.List;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.order.RefundOrderDO;
import com.chl.victory.dao.query.order.RefundOrderQuery;

public interface RefundOrderMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(RefundOrderDO record);

    //int insertSelective(RefundOrderDO record);

    /*RefundOrderDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RefundOrderDO record);*/

    //int updateByPrimaryKey(RefundOrderDO record);
    List<Long> selectRefundIds(RefundOrderQuery refundOrderQuery);

}