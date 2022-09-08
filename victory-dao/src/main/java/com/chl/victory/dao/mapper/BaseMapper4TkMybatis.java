package com.chl.victory.dao.mapper;

import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author ChenHailong
 * @date 2019/4/30 11:11
 **/
@RegisterMapper
public interface BaseMapper4TkMybatis<T> extends Mapper<T>, IdsMapper<T> {
}
