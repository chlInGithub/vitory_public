package com.chl.victory.dao.mapper;

import java.util.List;

import com.chl.victory.dao.model.StatusCountDO;

/**
 * @author ChenHailong
 * @date 2019/4/30 11:11
 **/
public interface BaseMapper<D, Q> {

    List<D> select(Q query);
    List<D> selectOutline(Q query);

    int count(Q query);

    D selectOne(Q query);

    int delete(Q query);

    int update(D model);

    int insert(Q query);

    List<StatusCountDO> countStatus(Q query);
}
