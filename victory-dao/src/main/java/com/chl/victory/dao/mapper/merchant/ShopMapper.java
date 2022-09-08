package com.chl.victory.dao.mapper.merchant;

import java.math.BigDecimal;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.merchant.ShopDO;

public interface ShopMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(ShopDO record);

    BigDecimal selectFreightFree(Long id);

    int updateFreightFree(ShopDO shopDO);

    String selectDeliveryArea(Long shopId);
    String selectPayType(Long shopId);
    String selectDeliveryType(Long shopId);

    int updateDeliveryArea(ShopDO shopDO);

    int updatePayType(ShopDO shopDO);

    int updateDeliveryType(ShopDO shopDO);

    //int insertSelective(ShopDO record);

    /*ShopDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShopDO record);*/

    //int updateByPrimaryKey(ShopDO record);
}