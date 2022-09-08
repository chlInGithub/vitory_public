package com.chl.victory.dao.mapper.activity;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.activity.ShopActivityDO;

public interface ShopActivityMapper extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(ShopActivityDO record);

    /*int insertSelective(ShopActivityDO record);

    ShopActivityDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShopActivityDO record);

    int updateByPrimaryKey(ShopActivityDO record);*/
}