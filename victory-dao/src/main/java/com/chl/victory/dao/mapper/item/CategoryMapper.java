package com.chl.victory.dao.mapper.item;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.item.CategoryDO;

public interface CategoryMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(CategoryDO record);

    //int insertSelective(CategoryDO record);

    /*CategoryDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CategoryDO record);*/

    //int updateByPrimaryKey(CategoryDO record);
}