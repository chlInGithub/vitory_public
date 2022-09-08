package com.chl.victory.dao.manager.coupons;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.coupons.ShopCouponsMapper;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;
import com.chl.victory.dao.query.coupons.ShopCouponsQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 店铺优惠券数据层访问入口
 *
 * @author ChenHailong
 * @date 2019/4/30 14:30
 **/
@Component
public class ShopCouponsManager extends BaseManager4Mybatis {
    @Resource
    ShopCouponsMapper shopCouponsMapper;

    public int save(ShopCouponsDO model) throws DaoManagerException {
        if (model.getShopId() == null){
            throw new DaoManagerException("缺少shopId");
        }

        ShopCouponsQuery checkNotExist4Insert = null;
        ShopCouponsQuery checkOnlyOne4Update = null;
        if (model.getId() == null){
        }else {
            checkOnlyOne4Update = new ShopCouponsQuery();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setShopId(model.getShopId());
        }

        return save(shopCouponsMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SHOP_COUPONS);
    }

    public int del(ShopCouponsQuery query) throws DaoManagerException {
            return del(shopCouponsMapper, query);
    }

    public List<ShopCouponsDO> select(ShopCouponsQuery query) throws DaoManagerException {
            return select(shopCouponsMapper, query);
    }

    public boolean exist(ShopCouponsQuery query) throws DaoManagerException {
        return count(shopCouponsMapper, query) > 0 ? true : false;
    }

    public int count(ShopCouponsQuery query) throws DaoManagerException {
        return count(shopCouponsMapper, query);
    }
}
