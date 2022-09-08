package com.chl.victory.service.services.coupons;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.coupons.ShopCouponsManager;
import com.chl.victory.dao.manager.coupons.UserCouponsManager;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;
import com.chl.victory.dao.model.coupons.UserCouponsDO;
import com.chl.victory.dao.query.coupons.ShopCouponsQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.service.utils.BeanUtils;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum;
import com.chl.victory.serviceapi.coupons.model.UserCouponsDTO;
import com.chl.victory.serviceapi.coupons.query.UserCouponsQueryDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @author ChenHailong
 * @date 2019/5/9 11:05
 **/
@Service
@Validated
public class UserCouponsService extends BaseService {

    @Resource
    ShopCouponsManager shopCouponsManager;

    @Resource
    UserCouponsManager userCouponsManager;

    public ServiceResult gain(UserCouponsDTO userCouponsDTO) {
        Long shopId = userCouponsDTO.getShopId();
        Long userId = userCouponsDTO.getUserId();
        Long couId = userCouponsDTO.getCouponsId();

        ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
        shopCouponsQuery.setId(couId);
        shopCouponsQuery.setShopId(shopId);
        shopCouponsQuery.setStatus(ActivityConponsStatusEnum.valid.getVal());
        shopCouponsQuery.setEndValidTime(new Date());
        List<ShopCouponsDO> shopCouponResult = null;
        try {
            shopCouponResult = shopCouponsManager.select(shopCouponsQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (CollectionUtils.isEmpty(shopCouponResult)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"优惠券不存在");
        }

        ShopCouponsDO shopCouponsDO = shopCouponResult.get(0);

        UserCouponsDO userCoupons = new UserCouponsDO();
        userCoupons.setShopId(shopId);
        userCoupons.setUserId(userId);
        userCoupons.setCouponsId(couId);
        userCoupons.setStatus(false);
        userCoupons.setExpiryTime(new Date());
        int userCouponsResult = 0;
        try {
            userCouponsResult = userCouponsManager.count(userCoupons);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (userCouponsResult > 0) {
            return ServiceResult.success();
        }

        userCoupons.setExpiryTime(shopCouponsDO.getInvalidTime());
        userCoupons.setOperatorId(userId);
        int saveResult = 0;
        try {
            saveResult = userCouponsManager.save(userCoupons);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (saveResult < 1) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "获取失败");
        }

        return ServiceResult.success();
    }

    public ServiceResult save(@NotNull UserCouponsDO userCouponsDO) {
        try {
            ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
            shopCouponsQuery.setId(userCouponsDO.getCouponsId());
            shopCouponsQuery.setShopId(userCouponsDO.getShopId());
            boolean exist = shopCouponsManager.exist(shopCouponsQuery);
            if (!exist) {
                return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "优惠券不存在");
            }

            userCouponsManager.save(userCouponsDO);
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<UserCouponsDTO>> select(@NotNull UserCouponsQueryDTO queryDTO) {
        try {
            UserCouponsDO query = BeanUtils.copyProperties(queryDTO, UserCouponsDO.class);
            List<UserCouponsDO> userCouponsDOS = userCouponsManager.select(query);
            List<UserCouponsDTO> userCouponsDTOS = toDTOs(userCouponsDOS, UserCouponsDTO.class);
            return ServiceResult.success(userCouponsDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<Integer> count(@NotNull UserCouponsQueryDTO queryDTO) {
        try {
            UserCouponsDO query = BeanUtils.copyProperties(queryDTO, UserCouponsDO.class);
            return ServiceResult.success(userCouponsManager.count(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
}
