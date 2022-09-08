package com.chl.victory.dao.manager.merchant;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.mapper.merchant.MerchantShopMapper;
import com.chl.victory.dao.mapper.merchant.MerchantUserMapper;
import com.chl.victory.dao.mapper.merchant.ShopMapper;
import com.chl.victory.dao.model.merchant.MerchantShopDO;
import com.chl.victory.dao.model.merchant.MerchantUserDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.query.BaseQuery;
import com.chl.victory.dao.query.merchant.MerchantShopQuery;
import com.chl.victory.dao.query.merchant.MerchantUserQuery;
import com.chl.victory.dao.query.merchant.ShopQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 商业用户相关数据数据层访问入口，包括商业用户  店铺 等
 *
 * @author ChenHailong
 * @date 2019/5/5 11:34
 **/
@Component
public class MerchantManager extends BaseManager4Mybatis {
    @Resource
    ShopMapper shopMapper;
    @Resource
    MerchantShopMapper merchantShopMapper;
    @Resource
    MerchantUserMapper merchantUserMapper;

    /**
     * 保存店铺基本信息，若新增店铺则创建店铺-店主关系
     *
     * @param model
     * @return
     * @throws DaoManagerException
     */
    public int saveShop(ShopDO model) throws DaoManagerException {
        if (StringUtils.isEmpty(model.getName()) || model.getMobile() == null || model.getOperatorId() == null) {
            throw new DaoManagerException("缺少名称或手机号或操作者");
        }

        ShopQuery checkNotExist4Insert = null;
        ShopQuery checkOnlyOne4Update = null;
        boolean isInsert = model.getId() == null;
        if (isInsert) {
            checkNotExist4Insert = new ShopQuery();
            checkNotExist4Insert.setName(model.getName());
        } else {
            checkOnlyOne4Update = new ShopQuery();
            checkOnlyOne4Update.setId(model.getId());
            //checkOnlyOne4Update.setName(model.getName());
        }

        int result = save(shopMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SHOP);

        if (result < 1) {
            return result;
        }

        if (isInsert) {
            MerchantShopDO merchantShopDO = new MerchantShopDO();
            merchantShopDO.setMerchantId(model.getOperatorId());
            merchantShopDO.setShopId(model.getId());
            merchantShopDO.setOperatorId(model.getOperatorId());
            save(merchantShopMapper, merchantShopDO, null, null, TableNameEnum.MERCHANT_SHOP);
        }

        return result;
    }

    /**
     * @param query 需要ID和手机号
     * @return
     * @throws DaoManagerException
     */
    public int delShop(ShopQuery query) throws DaoManagerException {
        if (query.getId() == null || query.getMobile() == null) {
            throw new DaoManagerException("缺少ID和手机号");
        }
        MerchantShopQuery merchantShopQuery = new MerchantShopQuery();
        merchantShopQuery.setShopId(query.getId());
        int result;
        if ((result = del(shopMapper, query)) > 0) {
            del(merchantShopMapper, merchantShopQuery);
        }
        return result;
    }

    /**
     * @param query
     * @param merchantId not null时，则查询归属该店主的店铺
     * @return
     * @throws DaoManagerException
     */
    public List<ShopDO> selectShops(ShopQuery query, Long merchantId) throws DaoManagerException {
        if (null != merchantId) {
            // 归属店主的店铺
            MerchantShopQuery merchantShopQuery = new MerchantShopQuery();
            merchantShopQuery.setMerchantId(merchantId);
            List<MerchantShopDO> merchantShopDOS = select(merchantShopMapper, merchantShopQuery);
            if (CollectionUtils.isEmpty(merchantShopDOS)) {
                return Collections.EMPTY_LIST;
            }

            return merchantShopDOS.stream().map(e -> {
                query.setId(e.getShopId());
                try {
                    return (ShopDO) selectOne(shopMapper, query);
                } catch (DaoManagerException ex) {
                    ex.printStackTrace();
                }
                return null;
            }).filter(e -> null != e).collect(Collectors.toList());
        } else {
            return select(shopMapper, query);
        }
    }

    /**
     * @param query
     * @return
     * @throws DaoManagerException
     * @see BaseManager4Mybatis#selectOne(BaseMapper, BaseQuery)
     */
    public ShopDO selectShop(ShopQuery query) throws DaoManagerException {
        return selectOne(shopMapper, query);
    }


    public int saveUser(MerchantUserDO model) throws DaoManagerException {
        if (model.getMobile() == null) {
            throw new DaoManagerException("缺少手机号");
        }

        ShopQuery checkNotExist4Insert = null;
        ShopQuery checkOnlyOne4Update = null;
        if (model.getId() == null) {
            checkNotExist4Insert = new ShopQuery();
            checkNotExist4Insert.setMobile(model.getMobile());
        } else {
            checkOnlyOne4Update = new ShopQuery();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setMobile(model.getMobile());
        }

        return save(merchantUserMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.MERCHANT_USER);
    }

    /**
     * @param query 需要ID和手机号
     * @return
     * @throws DaoManagerException
     */
    public int delUser(MerchantUserQuery query) throws DaoManagerException {
        if (query.getId() == null || query.getMobile() == null) {
            throw new DaoManagerException("缺少ID和手机号");
        }
        return del(merchantUserMapper, query);
    }

    public List<MerchantUserDO> selectUsers(MerchantUserQuery query) throws DaoManagerException {
        return select(merchantUserMapper, query);
    }

    public MerchantUserDO selectUser(MerchantUserQuery query) throws DaoManagerException {
        return selectOne(merchantUserMapper, query);
    }

    public BigDecimal selectFreightFree(Long shopId) {
        return shopMapper.selectFreightFree(shopId);
    }

    public int saveFreightFree(BigDecimal freightFree, Long shopId) {
        ShopDO shopDO = new ShopDO();
        shopDO.setId(shopId);
        shopDO.setFreightFree(freightFree);
        return shopMapper.updateFreightFree(shopDO);
    }

    public String selectDeliveryArea(Long shopId) {
        return shopMapper.selectDeliveryArea(shopId);
    }

    public int saveDeliveryArea(String deliveryArea, Long shopId) {
        ShopDO shopDO = new ShopDO();
        shopDO.setId(shopId);
        shopDO.setDeliveryArea(deliveryArea);
        return shopMapper.updateDeliveryArea(shopDO);
    }

    public int savePayType(List<Integer> payTypes, Long shopId) {
        ShopDO shopDO = new ShopDO();
        shopDO.setId(shopId);
        shopDO.setPayType(JSONObject.toJSONString(payTypes));
        return shopMapper.updatePayType(shopDO);
    }
    public int saveDeliveryType(List<Integer> deliveryTypes, Long shopId) {
        ShopDO shopDO = new ShopDO();
        shopDO.setId(shopId);
        shopDO.setDeliveryType(JSONObject.toJSONString(deliveryTypes));
        return shopMapper.updateDeliveryType(shopDO);
    }

    public String selectShopPayType(Long shopId) {
        return shopMapper.selectPayType(shopId);
    }

    public String selectShopDeliveryType(Long shopId) {
        return shopMapper.selectDeliveryType(shopId);
    }

    public int countMerchantShop(MerchantShopQuery query) {
        return merchantShopMapper.count(query);
    }

    public int countMerchant(MerchantUserQuery query) {
        return merchantUserMapper.count(query);
    }
}
