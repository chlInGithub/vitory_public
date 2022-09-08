package com.chl.victory.dao.mapper.merchant;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.merchant.MerchantUserDO;

public interface MerchantUserMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(MerchantUserDO record);

    //int insertSelective(MerchantUserDO record);

    /*MerchantUserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MerchantUserDO record);*/

    //int updateByPrimaryKey(MerchantUserDO record);
}