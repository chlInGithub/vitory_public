package com.chl.victory.dao.mapper.merchant;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.merchant.MerchantShopDO;

public interface MerchantShopMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(MerchantShopDO record);

    //int insertSelective(MerchantShopDO record);

    /*MerchantShopDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MerchantShopDO record);*/

    //int updateByPrimaryKey(MerchantShopDO record);
}