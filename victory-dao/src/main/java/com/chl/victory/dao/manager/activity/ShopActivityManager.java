package com.chl.victory.dao.manager.activity;

import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.activity.ShopActivityMapper;
import com.chl.victory.dao.model.activity.ShopActivityDO;
import com.chl.victory.dao.query.activity.ShopActivityQuery;
import org.springframework.stereotype.Component;

/**
 * 店铺活动数据层访问入口
 * @author ChenHailong
 * @date 2019/4/30 14:30
 **/
@Component
public class ShopActivityManager extends BaseManager4Mybatis {

    @Resource
    ShopActivityMapper shopActivityMapper;

    public int save(ShopActivityDO model) throws DaoManagerException {
        if (model.getShopId() == null) {
            throw new DaoManagerException("缺少shopId");
        }

        ShopActivityQuery checkNotExist4Insert = null;
        ShopActivityQuery checkOnlyOne4Update = null;
        if (model.getId() == null) {
        }
        else {
            checkOnlyOne4Update = new ShopActivityQuery();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setShopId(model.getShopId());
        }

        return save(shopActivityMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SHOP_ACTIVITY);
    }

    public int del(ShopActivityQuery query) throws DaoManagerException {
        return del(shopActivityMapper, query);
    }

    public List<ShopActivityDO> select(ShopActivityQuery query) throws DaoManagerException {
        return select(shopActivityMapper, query);
    }

    public int count(ShopActivityQuery query) throws DaoManagerException {
        return count(shopActivityMapper, query);
    }
}
