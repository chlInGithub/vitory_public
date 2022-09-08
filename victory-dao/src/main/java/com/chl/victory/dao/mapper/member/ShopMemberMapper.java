package com.chl.victory.dao.mapper.member;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.member.ShopMemberDO;

public interface ShopMemberMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(ShopMemberDO record);

    //int insertSelective(ShopMemberDO record);

    /*ShopMemberDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShopMemberDO record);*/

    //int updateByPrimaryKey(ShopMemberDO record);
}