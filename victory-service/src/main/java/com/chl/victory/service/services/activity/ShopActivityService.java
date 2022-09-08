package com.chl.victory.service.services.activity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheService;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.activity.ShopActivityManager;
import com.chl.victory.dao.model.activity.ShopActivityDO;
import com.chl.victory.dao.query.activity.ShopActivityQuery;
import com.chl.victory.localservice.ShopActivityLocalService;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.activity.model.ShopActivityDTO;
import com.chl.victory.serviceapi.activity.query.ShopActivityQueryDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author ChenHailong
 * @date 2019/5/9 11:05
 **/
@Service
@Validated
public class ShopActivityService extends BaseService {

    @Resource
    ShopActivityManager shopActivityManager;

    @Resource
    CacheService cacheService;

    @Resource
    ShopActivityLocalService shopActivityLocalService;

    public ServiceResult saveActivity(@NotNull ShopActivityDTO shopActivityDTO, List<Long> itemIds) {
        try {
            ShopActivityDO shopActivityDO = toDO(shopActivityDTO, ShopActivityDO.class);
            int saveCount = shopActivityManager.save(shopActivityDO);
            if (saveCount > 0) {
                Long shopId = shopActivityDO.getShopId();
                Long id = shopActivityDO.getId();
                shopActivityLocalService.saveActivityItems(shopId, id, itemIds);

                shopActivityLocalService.delActivityCache(shopId, shopActivityDO.getId().toString());
            }
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<ShopActivityDTO>> selectActivity(@NotNull ShopActivityQueryDTO queryDTO) {
        try {
            ShopActivityQuery query = toQuery(queryDTO, ShopActivityQuery.class);
            List<ShopActivityDO> shopActivityDOS = shopActivityManager.select(query);
            List<ShopActivityDTO> shopActivityDTOS = toDTOs(shopActivityDOS, ShopActivityDTO.class);
            return ServiceResult.success(shopActivityDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 活动是否有效
     */
    public boolean isValid(@NotNull ShopActivityDO shopActivityDO, @NotNull Date date) {
        boolean isValid =
                null != shopActivityDO && null != date && shopActivityDO.getStatus() && shopActivityDO.getValidTime()
                        .before(date) && shopActivityDO.getInvalidTime().after(date);
        return isValid;
    }

    /**
     * 优惠金额
     * @param shopActivityDO
     * @param activityItemTotalFee
     * @return
     */
    public BigDecimal deduct(@NotNull ShopActivityDO shopActivityDO, @NotNull BigDecimal activityItemTotalFee) {
        if (shopActivityDO.getMeet().compareTo(activityItemTotalFee) > 0) {
            return BigDecimal.ZERO;
        }

        if (shopActivityDO.getRepeat()) {
            BigDecimal discountNum = activityItemTotalFee.divide(shopActivityDO.getMeet(), 2, BigDecimal.ROUND_DOWN)
                    .setScale(0, BigDecimal.ROUND_DOWN);
            return shopActivityDO.getDiscount().multiply(discountNum);
        }

        return shopActivityDO.getDiscount();
    }

    public ServiceResult<Integer> count(@NotNull ShopActivityQueryDTO queryDTO) {
        try {
            ShopActivityQuery query = toQuery(queryDTO, ShopActivityQuery.class);
            return ServiceResult.success(shopActivityManager.count(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult invalid(Long id, Long shopId) {
        // 更改数据库
        ShopActivityDO shopActivityDO = new ShopActivityDO();
        shopActivityDO.setShopId(shopId);
        shopActivityDO.setId(id);
        shopActivityDO.setStatus(ActivityConponsStatusEnum.invalid.getVal());
        try {
            shopActivityManager.save(shopActivityDO);

            shopActivityLocalService.delActivityCache(shopId, id.toString());

            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult valid(Long id, Long shopId) {
        // 更改数据库
        ShopActivityDO shopActivityDO = new ShopActivityDO();
        shopActivityDO.setShopId(shopId);
        shopActivityDO.setId(id);
        shopActivityDO.setStatus(ActivityConponsStatusEnum.valid.getVal());
        try {
            shopActivityManager.save(shopActivityDO);

            shopActivityLocalService.delActivityCache(shopId, id.toString());

            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult del(Long id, Long shopId) {
        ShopActivityQuery query = new ShopActivityQuery();
        query.setShopId(shopId);
        query.setId(id);
        try {
            shopActivityManager.del(query);

            shopActivityLocalService.delActivityCache(shopId, id.toString());
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        // 更新cache
        shopActivityLocalService.delActivityItems(shopId, id);

        return ServiceResult.success();
    }
}
