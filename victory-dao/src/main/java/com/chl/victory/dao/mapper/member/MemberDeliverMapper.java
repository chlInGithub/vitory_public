package com.chl.victory.dao.mapper.member;

import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.member.MemberDeliverDO;

public interface MemberDeliverMapper  extends BaseMapper {
    //int deleteByPrimaryKey(Long id);

    int insert(MemberDeliverDO record);

    //int insertSelective(MemberDeliverDO record);

    /*MemberDeliverDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MemberDeliverDO record);*/

    //int updateByPrimaryKey(MemberDeliverDO record);
}