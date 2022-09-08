package com.chl.victory.serviceapi.merchant.utils;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.model.PayConfigDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;

/**
 * @author ChenHailong
 * @date 2020/9/2 15:23
 **/
public class MerchantUtils {
    public PayConfigDTO getPayConfig(@NotNull ShopAppDTO shopAppDO) throws BusServiceException {
        String payConfig = shopAppDO.getPayConfig();
        if (null == payConfig || payConfig.trim().isEmpty()) {
            throw new BusServiceException("缺少支付配置1" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        PayConfigDTO parsedPayConfig = PayConfigDTO.parse(payConfig);
        if (null == parsedPayConfig) {
            throw new BusServiceException("解析失败支付配置2" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        if (null == parsedPayConfig.getMchId() || parsedPayConfig.getMchId().trim().isEmpty()) {
            throw new BusServiceException("缺少支付配置2" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        if (!parsedPayConfig.isSub() && (null == parsedPayConfig.getApiKey() || parsedPayConfig.getApiKey().trim().isEmpty())) {
            throw new BusServiceException("缺少支付配置3" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        return parsedPayConfig;
    }
}
