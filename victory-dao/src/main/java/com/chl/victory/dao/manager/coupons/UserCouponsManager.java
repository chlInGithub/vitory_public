package com.chl.victory.dao.manager.coupons;

import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.coupons.UserCouponsMapper;
import com.chl.victory.dao.model.coupons.UserCouponsDO;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * 用户的优惠券数据层访问入口
 * @author ChenHailong
 * @date 2019/4/30 14:30
 **/
@Component
public class UserCouponsManager extends BaseManager4TkMybatis {

    @Resource
    UserCouponsMapper userCouponsMapper;

    public int save(UserCouponsDO model) throws DaoManagerException {
        if (model.getShopId() == null) {
            throw new DaoManagerException("缺少shopId");
        }

        UserCouponsDO checkNotExist4Insert = null;
        UserCouponsDO checkOnlyOne4Update = null;
        if (model.getId() == null) {
        }
        else {
            checkOnlyOne4Update = new UserCouponsDO();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setShopId(model.getShopId());
            checkOnlyOne4Update.setCouponsId(model.getCouponsId());
            checkOnlyOne4Update.setUserId(model.getUserId());
        }

        return save(userCouponsMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SHOP_COUPONS);
    }

    public int del(UserCouponsDO query) throws DaoManagerException {
        return del(userCouponsMapper, query);
    }

    public List<UserCouponsDO> select(UserCouponsDO query) throws DaoManagerException {
        if (query.getExpiryTime() != null) {
            Example example = new Example(UserCouponsDO.class);
            Example.Criteria criteria = example.createCriteria();
            if (query.getUserId() != null) {
                criteria.andEqualTo("userId", query.getUserId());
            }
            if (query.getCouponsId() != null) {
                criteria.andEqualTo("couponsId", query.getCouponsId());
            }
            if (query.getStatus() != null) {
                criteria.andEqualTo("status", query.getStatus());
            }
            criteria.andGreaterThanOrEqualTo("expiryTime", query.getExpiryTime());
            List<UserCouponsDO> userCouponsDOS = userCouponsMapper.selectByExample(example);
            return userCouponsDOS;
        }
        else {
            List<UserCouponsDO> userCouponsDOS = select(userCouponsMapper, query);
            return userCouponsDOS;
        }
    }

    public int count(UserCouponsDO query) throws DaoManagerException {
        if (query.getExpiryTime() != null) {
            Example example = new Example(UserCouponsDO.class);
            Example.Criteria criteria = example.createCriteria();
            if (query.getUserId() != null) {
                criteria.andEqualTo("userId", query.getUserId());
            }
            if (query.getCouponsId() != null) {
                criteria.andEqualTo("couponsId", query.getCouponsId());
            }
            if (query.getStatus() != null) {
                criteria.andEqualTo("status", query.getStatus());
            }
            criteria.andGreaterThanOrEqualTo("expiryTime", query.getExpiryTime());
            int count = userCouponsMapper.selectCountByExample(example);
            return count;
        }
        else {
            int count = count(userCouponsMapper, query);
            return count;
        }
    }
}
