package com.chl.victory.serviceapi.merchant;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.model.PayConfigDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDeliveryAreaDTO;
import com.chl.victory.serviceapi.merchant.model.StyleConfigDTO;
import com.chl.victory.serviceapi.merchant.query.MerchantUserQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 18:17
 **/
public interface MerchantFacade {

    ServiceResult saveUser(MerchantUserDTO userDO);

    ServiceResult<ShopDTO> selectShop(Long shopId);

    ServiceResult saveShop(@NotNull ShopDTO shopDO);

    ServiceResult saveFreightFree(BigDecimal freightFree, Long shopId);

    ServiceResult saveDeliveryArea(String deliveryArea, Long shopId);

    ServiceResult savePayType(@NotEmpty List<Integer> payTypes, @NotNull Long shopId);
    ServiceResult saveDeliveryType(@NotEmpty List<Integer> deliveryTypes, @NotNull Long shopId);

    ServiceResult<Integer> countShopApp(@NotNull Long shopId);

    ServiceResult<BigDecimal> selectShopFreightFree(Long shopId);

    ServiceResult<List<Integer>> selectShopPayType(Long shopId);

    ServiceResult<List<ShopDeliveryAreaDTO>> selectShopDeliveryArea(Long shopId);

    ServiceResult<Long> getShopId(String appId);

    ServiceResult<ShopAppDTO> getShopAppCache(@NotNull Long shopId, @NotEmpty String appId) throws Exception;

    ServiceResult<List<ShopAppDTO>> selectShopApps(@NotNull Long shopId);

    ServiceResult<ShopAppDTO> selectShopAppWithValidate(@NotNull Long shopId, @NotEmpty String appId);

    ServiceResult saveShopApp(ShopAppDTO shopAppDO);

    ServiceResult<Integer> saveWXPayCert(Long shopId, String appId, byte[] cert);

    ServiceResult savePayConfig(@NotNull Long shopAppId, @NotEmpty String appId, @NotNull Long shopId,
            @NotNull PayConfigDTO payConfig);

    ServiceResult saveStyle(@NotNull Long shopAppId, @NotEmpty String appId, @NotNull Long shopId,
            @NotNull StyleConfigDTO config);

    ServiceResult<StyleConfigDTO> selectStyle(@NotEmpty String appId, @NotNull Long shopId);

    ServiceResult<Boolean> existWXPayCert(Long shopId, String appId);

    ServiceResult saveWeixinAuth(@NotNull Long shopId, @NotNull Long operatorId, @NotEmpty String authCode,
            boolean fastRegiste);

    ServiceResult<MerchantUserDTO> selectUser(MerchantUserQueryDTO userQuery);

    ServiceResult<List<ShopDTO>> selectShops(Long userId);

    boolean isExperienceShop(Long shopId);

    ServiceResult<List<Integer>> selectShopDeliveryType(Long shopId);

    ServiceResult<Boolean> isMerchantUser(MerchantUserQueryDTO merchantUserQueryDTO);
}
