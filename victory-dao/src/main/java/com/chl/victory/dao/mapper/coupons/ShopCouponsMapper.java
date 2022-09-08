package com.chl.victory.dao.mapper.coupons;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;

public interface ShopCouponsMapper extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(ShopCouponsDO record);

    //int insertSelective(ShopCouponsDO record);

    /*ShopCouponsDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShopCouponsDO record);*/

    //int updateByPrimaryKey(ShopCouponsDO record);
}