package com.chl.victory.service.services.coupons;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.chl.victory.common.redis.CacheService;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.coupons.ShopCouponsManager;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;
import com.chl.victory.dao.query.coupons.ShopCouponsQuery;
import com.chl.victory.localservice.ShopCouponsLocalService;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.coupons.model.ShopCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.ShopCouponsQueryDTO;
import org.springframework.stereotype.Service;

/**
 * @author ChenHailong
 * @date 2019/5/9 11:05
 **/
@Service
public class ShopCouponsService extends BaseService {

    @Resource
    ShopCouponsManager shopCouponsManager;

    @Resource
    CacheService cacheService;

    @Resource
    ShopCouponsLocalService shopCouponsLocalService;

    public ServiceResult save(ShopCouponsDTO dto, List<Long> itemIds) {
        try {
            ShopCouponsDO model = toDO(dto, ShopCouponsDO.class);
            int saveCount = shopCouponsManager.save(model);
            if (saveCount > 0) {
                Long shopId = model.getShopId();
                Long id = model.getId();

                shopCouponsLocalService.saveItemIdsOfCoupons(shopId, id, itemIds);
            }
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<ShopCouponsDTO>> select(ShopCouponsQueryDTO queryDTO) {
        try {
            ShopCouponsQuery query = toQuery(queryDTO, ShopCouponsQuery.class);
            List<ShopCouponsDO> shopCouponsDOS = shopCouponsManager.select(query);
            List<ShopCouponsDTO> shopCouponsDTOS = toDTOs(shopCouponsDOS, ShopCouponsDTO.class);
            return ServiceResult.success(shopCouponsDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public List<Long> selectCouponItemIds(Long shopId, Long couId, List<Long> buyedItemIds) {
        String key = shopCouponsLocalService.getCouponItemsCacheKey(shopId, couId);
        boolean existKey = cacheService.existsKey(key);
        // key 不存在，表示所有商品
        if (!existKey) {
            return buyedItemIds;
        }

        buyedItemIds = buyedItemIds.stream().filter(item -> cacheService.sIsMember(key, item))
                .collect(Collectors.toList());
        return buyedItemIds;
    }

    public BigDecimal deduct(ShopCouponsDO shopCouponsDO, BigDecimal itemTotalFee) {
        if (shopCouponsDO.getMeet().compareTo(itemTotalFee) > 0) {
            return BigDecimal.ZERO;
        }

        return shopCouponsDO.getDiscount();
    }

    public ServiceResult<Integer> count(ShopCouponsQueryDTO queryDTO) {
        try {
            ShopCouponsQuery query = toQuery(queryDTO, ShopCouponsQuery.class);
            return ServiceResult.success(shopCouponsManager.count(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult invalid(Long id, Long shopId) {
        // 更改数据库
        ShopCouponsDO shopActivityDO = new ShopCouponsDO();
        shopActivityDO.setShopId(shopId);
        shopActivityDO.setId(id);
        shopActivityDO.setStatus(ActivityConponsStatusEnum.invalid.getVal());
        try {
            shopCouponsManager.save(shopActivityDO);
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult valid(Long id, Long shopId) {
        // 更改数据库
        ShopCouponsDO shopActivityDO = new ShopCouponsDO();
        shopActivityDO.setShopId(shopId);
        shopActivityDO.setId(id);
        shopActivityDO.setStatus(ActivityConponsStatusEnum.valid.getVal());
        try {
            shopCouponsManager.save(shopActivityDO);
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult del(Long id, Long shopId) {
        ShopCouponsQuery query = new ShopCouponsQuery();
        query.setShopId(shopId);
        query.setId(id);
        try {
            shopCouponsManager.del(query);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        // 更新cache
        shopCouponsLocalService.delItemIdsOfCoupons(shopId, id);

        return ServiceResult.success();
    }
}
