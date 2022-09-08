package com.chl.victory.dao.mapper.item;

import java.util.List;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.query.item.ItemQuery;

public interface ItemMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(ItemDO record);

    int deductInventory(ItemDO itemDO);
    int verifyDeductInventory(ItemDO itemDO);

    int addInventory(ItemDO itemDO);
    int addSales(ItemDO itemDO);

    //int insertSelective(ItemDO record);

    /*ItemDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ItemDO record);*/

    //int updateByPrimaryKey(ItemDO record);

    List<ItemDO> selectOutline(ItemQuery query);
}