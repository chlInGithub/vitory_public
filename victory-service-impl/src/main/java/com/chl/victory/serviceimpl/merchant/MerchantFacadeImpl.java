package com.chl.victory.serviceimpl.merchant;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.PayConfig;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.StyleConfig;
import com.chl.victory.dao.query.merchant.MerchantShopQuery;
import com.chl.victory.service.utils.BeanUtils;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.MerchantFacade;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.model.PayConfigDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDeliveryAreaDTO;
import com.chl.victory.serviceapi.merchant.model.StyleConfigDTO;
import com.chl.victory.serviceapi.merchant.query.MerchantUserQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.experienceService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/9/2 9:57
 **/
@DubboService
public class MerchantFacadeImpl implements MerchantFacade {

    @Override
    public ServiceResult saveUser(MerchantUserDTO userDTO) {
        return merchantService.saveUser(userDTO);
    }

    @Override
    public ServiceResult<ShopDTO> selectShop(Long shopId) {
        try {
            return merchantService.selectShopDTO(shopId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult saveShop(@NotNull ShopDTO shopDTO) {
        return merchantService.saveShop(shopDTO);
    }

    @Override
    public ServiceResult saveFreightFree(BigDecimal freightFree, Long shopId) {
        return merchantService.saveFreightFree(freightFree, shopId);
    }

    @Override
    public ServiceResult saveDeliveryArea(String deliveryArea, Long shopId) {
        return merchantService.saveDeliveryArea(deliveryArea, shopId);
    }

    @Override
    public ServiceResult savePayType(@NotEmpty List<Integer> payTypes, @NotNull Long shopId) {
        return merchantService.savePayType(payTypes, shopId);
    }

    @Override
    public ServiceResult saveDeliveryType(@NotEmpty List<Integer> deliveryTypes, @NotNull Long shopId) {
        return merchantService.saveDeliveryType(deliveryTypes, shopId);
    }

    @Override
    public ServiceResult<Integer> countShopApp(@NotNull Long shopId) {
        try {
            return ServiceResult.success(merchantService.countShopApp(shopId));
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult<BigDecimal> selectShopFreightFree(Long shopId) {
        return merchantService.selectShopFreightFree(shopId);
    }

    @Override
    public ServiceResult<List<Integer>> selectShopPayType(Long shopId) {
        return merchantService.selectShopPayType(shopId);
    }

    @Override
    public ServiceResult<List<ShopDeliveryAreaDTO>> selectShopDeliveryArea(Long shopId) {
        return merchantService.selectShopDeliveryArea(shopId);
    }

    @Override
    public ServiceResult<Long> getShopId(String appId) {
        try {
            return ServiceResult.success(merchantService.getShopId(appId));
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult<ShopAppDTO> getShopAppCache(@NotNull Long shopId, @NotEmpty String appId) {
        ShopAppDO shopAppDO;
        try {
            shopAppDO = merchantService.getShopAppCache(shopId, appId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        ShopAppDTO shopAppDTO = BeanUtils.copyProperties(shopAppDO, ShopAppDTO.class);
        return ServiceResult.success(shopAppDTO);
    }

    @Override
    public ServiceResult<List<ShopAppDTO>> selectShopApps(@NotNull Long shopId) {
        List<ShopAppDO> shopAppDOS;
        try {
            shopAppDOS = merchantService.selectShopApps(shopId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        List<ShopAppDTO> shopAppDTOS = BeanUtils.copyList(shopAppDOS, ShopAppDTO.class);
        return ServiceResult.success(shopAppDTOS);
    }

    @Override
    public ServiceResult<ShopAppDTO> selectShopAppWithValidate(@NotNull Long shopId, @NotEmpty String appId) {
        ShopAppDO shopAppDO;
        try {
            shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        ShopAppDTO shopAppDTO = BeanUtils.copyProperties(shopAppDO, ShopAppDTO.class);
        return ServiceResult.success(shopAppDTO);
    }

    @Override
    public ServiceResult saveShopApp(ShopAppDTO shopAppDTO){
        ShopAppDO shopAppDO = BeanUtils.copyProperties(shopAppDTO, ShopAppDO.class);
        try {
            merchantService.saveShopApp(shopAppDO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult<Integer> saveWXPayCert(Long shopId, String appId, byte[] cert) {
        return merchantService.saveWXPayCert(shopId, appId, cert);
    }

    @Override
    public ServiceResult savePayConfig(@NotNull Long shopAppId, @NotEmpty String appId, @NotNull Long shopId, @NotNull PayConfigDTO payConfigDTO) {
        PayConfig payConfig = BeanUtils.copyProperties(payConfigDTO, PayConfig.class);
        try {
            merchantService.savePayConfig(shopAppId, appId, shopId, payConfig);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult saveStyle(@NotNull Long shopAppId, @NotEmpty String appId, @NotNull Long shopId,
            @NotNull StyleConfigDTO config) {
        StyleConfig payConfig = BeanUtils.copyProperties(config, StyleConfig.class);
        try {
            merchantService.saveStyleConfig(shopAppId, appId, shopId, payConfig);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult<StyleConfigDTO> selectStyle(@NotEmpty String appId, @NotNull Long shopId) {
        try {
            String style = merchantService.selectStyle(appId, shopId);
            StyleConfigDTO styleConfigDTO = StyleConfigDTO.parse(style);
            return ServiceResult.success(styleConfigDTO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ServiceResult<Boolean> existWXPayCert(Long shopId, String appId) {
        return merchantService.existWXPayCert(shopId, appId);
    }

    @Override
    public ServiceResult saveWeixinAuth(@NotNull Long shopId, @NotNull Long operatorId, @NotEmpty String authCode,
            boolean fastRegiste) {
        try {
            merchantService.saveWeixinAuth(shopId, operatorId, authCode, fastRegiste);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult<MerchantUserDTO> selectUser(MerchantUserQueryDTO userQuery) {
        return merchantService.selectUser(userQuery);
    }

    @Override
    public ServiceResult<List<ShopDTO>> selectShops(Long userId) {
        return merchantService.selectShops(userId);
    }

    @Override
    public boolean isExperienceShop(Long shopId) {
        return experienceService.isExperienceShop(shopId);
    }

    @Override
    public ServiceResult<List<Integer>> selectShopDeliveryType(Long shopId) {
        return merchantService.selectShopDeliveryType(shopId);
    }

    @Override
    public ServiceResult<Boolean> isMerchantUser(MerchantUserQueryDTO merchantUserQuery) {
        MerchantShopQuery merchantShopQuery = new MerchantShopQuery();
        merchantShopQuery.setShopId(merchantUserQuery.getShopId());
        merchantShopQuery.setMerchantId(merchantUserQuery.getId());
        ServiceResult<Boolean> existMerchantShop = merchantService.existMerchantShop(merchantShopQuery);
        if (existMerchantShop.getData() && merchantUserQuery.getMobile() != null) {
            ServiceResult<Boolean> existMerchant = merchantService.existMerchant(merchantUserQuery);
            return existMerchant;
        }
        return existMerchantShop;
    }
}
