package com.chl.victory.dao.mapper.item;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.item.SkuDO;

public interface SkuMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(SkuDO record);

    int deductInventory(SkuDO skuDO);
    int verifyDeductInventory(SkuDO skuDO);

    int addInventory(SkuDO skuDO);
    int addSales(SkuDO skuDO);

    //int insertSelective(SkuDO record);

    /*SkuDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SkuDO record);*/

    //int updateByPrimaryKey(SkuDO record);
}