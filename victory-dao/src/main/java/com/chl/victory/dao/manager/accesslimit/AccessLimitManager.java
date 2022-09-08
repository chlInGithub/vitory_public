package com.chl.victory.dao.manager.accesslimit;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.accesslimit.AccessLimitMapper;
import com.chl.victory.dao.model.accesslimit.AccessLimitDO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/4/10 16:17
 **/
@Component
public class AccessLimitManager extends BaseManager4TkMybatis {
    @Resource
    AccessLimitMapper accessLimitMapper;

    public int save(AccessLimitDO model) throws DaoManagerException {
        if (model.getShopId() == null){
            throw new DaoManagerException("缺少shopId");
        }

        AccessLimitDO checkNotExist4Insert = null;
        AccessLimitDO checkOnlyOne4Update = null;
        if (model.getId() == null){
            if (model.getType() == null || model.getPeriod() == null || model.getMaxLimit() == null || model.getInvalidTime() == null){
                throw new DaoManagerException("缺少限流必须字段");
            }
        }else {
            checkOnlyOne4Update = new AccessLimitDO();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setShopId(model.getShopId());
        }

        return save(accessLimitMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SHOP_COUPONS);
    }

    public int del(AccessLimitDO query) throws DaoManagerException {
        return del(accessLimitMapper, query);
    }

    public List<AccessLimitDO> select(AccessLimitDO query) throws DaoManagerException {
        Date expiryTime = query.getInvalidTime();
        query.setInvalidTime(null);
        List<AccessLimitDO> accessLimitDOS = select(accessLimitMapper, query);
        if (expiryTime != null && !CollectionUtils.isEmpty(accessLimitDOS)) {
            accessLimitDOS = accessLimitDOS.stream().filter(accessLimitDO -> !accessLimitDO.getInvalidTime().before(expiryTime)).collect(Collectors
                    .toList());
        }
        return accessLimitDOS;
    }
    public int count(AccessLimitDO query) throws DaoManagerException {
        int count = count(accessLimitMapper, query);
        return count;
    }
}
