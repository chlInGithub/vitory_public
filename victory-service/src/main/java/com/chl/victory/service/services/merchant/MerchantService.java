package com.chl.victory.service.services.merchant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.merchant.MerchantManager;
import com.chl.victory.dao.manager.merchant.ShopAppManager;
import com.chl.victory.dao.model.merchant.MerchantUserDO;
import com.chl.victory.dao.model.merchant.PayConfig;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.model.merchant.StyleConfig;
import com.chl.victory.dao.query.merchant.MerchantShopQuery;
import com.chl.victory.dao.query.merchant.MerchantUserQuery;
import com.chl.victory.dao.query.merchant.ShopQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4BasicInfoService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDeliveryAreaDTO;
import com.chl.victory.serviceapi.merchant.query.MerchantUserQueryDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.AuthorizationInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.service.services.ServiceManager.authorizerService;
import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.componentService;
import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4BasicInfoService;
import static com.chl.victory.service.services.ServiceManager.infoService;

/**
 * ????????????????????????????????????????????????????????????
 * @author ChenHailong
 * @date 2019/5/7 16:34
 **/
@Service
@Validated
@Slf4j
public class MerchantService extends BaseService {

    @Resource
    MerchantManager merchantManager;

    @Resource
    ShopAppManager shopAppManager;

    /**
     * ????????????????????????domain???
     */
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    public ServiceResult saveShop(@NotNull ShopDTO shopDTO) {
        ShopDO shopDO = toDO(shopDTO, ShopDO.class);
        // ????????????????????????
        return _saveShop(shopDO);
    }

    public ServiceResult saveShop(@NotNull ShopDO shopDO, @NotNull ShopAppDO shopAppDO) {
        // ????????????????????????
        return _saveShop(shopDO, shopAppDO);
    }

    public ServiceResult saveUser(@NotNull MerchantUserDTO userDTO) {
        // ???????????????????????????????????????????????????????????????????????????
        /*if (null == userDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID);
        }*/
        MerchantUserDO userDO = toDO(userDTO, MerchantUserDO.class);

        // ????????????????????????
        return _saveUser(userDO);
    }

    public ServiceResult<List<ShopDTO>> selectShops(Long merchantId) {
        try {
            List<ShopDO> dos = merchantManager.selectShops(new ShopQuery(), merchantId);
            List<ShopDTO> shopDTOS = toDTOs(dos, ShopDTO.class);
            return ServiceResult.success(shopDTOS);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<ShopDO>> selectShops(ShopQuery query) {
        try {
            List<ShopDO> dos = merchantManager.selectShops(query, null);
            return ServiceResult.success(dos);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<ShopDTO> selectShopDTO(Long shopId) throws BusServiceException {
        ShopDO shopDO = selectShop(shopId);
        ShopDTO shopDTO = toDTO(shopDO, ShopDTO.class);
        return ServiceResult.success(shopDTO);
    }

    public ShopDO selectShop(Long shopId) throws BusServiceException {
        ShopQuery shopQuery = new ShopQuery();
        shopQuery.setShopId(shopId);
        ServiceResult<List<ShopDO>> selectShops = selectShops(shopQuery);

        if (!selectShops.getSuccess()) {
            throw new BusServiceException("??????" + shopId + "????????????," + selectShops.getMsg());
        }
        if (CollectionUtils.isEmpty(selectShops.getData()) || selectShops.getData().get(0) == null) {
            throw new BusServiceException("??????" + shopId + "?????????");
        }

        ShopDO shopDO = selectShops.getData().get(0);

        return shopDO;
    }

    /**
     * ????????????app????????????????????????null
     * @param shopId
     * @param appId
     * @return
     * @throws BusServiceException
     */
    public ShopAppDO selectShopApp(@NotNull Long shopId, @NotEmpty String appId) throws BusServiceException {

        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setShopId(shopId);
        shopAppDO.setAppId(appId);
        List<ShopAppDO> shopAppDOS;
        try {
            shopAppDOS = shopAppManager.selectApps(shopAppDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException("????????????app????????????" + shopId + "|" + appId, e);
        }

        if (CollectionUtils.isEmpty(shopAppDOS)) {
            return null;
            // throw new NotExistException("??????app???????????????" + shopId + "|" + appId);
        }

        return shopAppDOS.get(0);
    }

    public int countShopApp(@NotNull Long shopId) throws BusServiceException {
        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setShopId(shopId);
        try {
            return shopAppManager.count(shopAppDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException("??????????????????app????????????" + shopId, e);
        }
    }

    /**
     * ???????????????????????????app
     * @param shopId
     * @return
     * @throws BusServiceException
     */
    public List<ShopAppDO> selectShopApps(@NotNull Long shopId) throws BusServiceException {

        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setShopId(shopId);
        List<ShopAppDO> shopAppDOS;
        try {
            shopAppDOS = shopAppManager.selectApps(shopAppDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException("????????????app????????????" + shopId, e);
        }

        if (null == shopAppDOS) {
            shopAppDOS = new ArrayList<>();
        }

        return shopAppDOS;
    }

    public ShopAppDO selectShopAppWithCheckExist(@NotNull Long shopId, @NotEmpty String appId)
            throws BusServiceException {

        ShopAppDO shopAppDO = selectShopApp(shopId, appId);
        if (null == shopAppDO) {
            throw new NotExistException("??????app???????????????" + shopId + "|" + appId);
        }

        return shopAppDO;
    }

    public ShopAppDO selectShopAppWithValidate(@NotNull Long shopId, @NotEmpty String appId)
            throws BusServiceException {

        ShopAppDO shopAppDO = selectShopAppWithCheckExist(shopId, appId);

        if (StringUtils.isBlank(shopAppDO.getDomain())) {
            throw new BusServiceException("????????????" + appId);
        }

        if (StringUtils.isBlank(shopAppDO.getAuthRefreshToken())) {
            throw new BusServiceException("??????????????????" + appId);
        }

        return shopAppDO;
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param shopAppDO
     * @return
     */
    public boolean isAuthOrFastRegiste(ShopAppDO shopAppDO) {
        return shopAppDO.getFastRegiste() != null && (shopAppDO.getFastRegiste() == 0
                || shopAppDO.getFastRegiste() == 1);
    }

    /**
     * ??????shopappdo???payconfig??????
     * @param shopAppDO
     * @return
     * @throws BusServiceException
     */
    public PayConfig getPayConfig(@NotNull ShopAppDO shopAppDO) throws BusServiceException {
        String payConfig = shopAppDO.getPayConfig();
        if (StringUtils.isBlank(payConfig)) {
            throw new BusServiceException("??????????????????1" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        PayConfig parsedPayConfig = PayConfig.parse(payConfig);
        if (null == parsedPayConfig) {
            throw new BusServiceException("????????????????????????2" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        if (StringUtils.isBlank(parsedPayConfig.getMchId())) {
            throw new BusServiceException("??????????????????2" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        if (!parsedPayConfig.isSub() && StringUtils.isBlank(parsedPayConfig.getApiKey())) {
            throw new BusServiceException("??????????????????3" + shopAppDO.getShopId() + "|" + shopAppDO.getAppId());
        }

        return parsedPayConfig;
    }

    public ServiceResult<List<MerchantUserDO>> selectUsers(MerchantUserQuery query) {
        try {
            List<MerchantUserDO> dos = merchantManager.selectUsers(query);
            return ServiceResult.success(dos);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<MerchantUserDTO> selectUser(MerchantUserQueryDTO queryDTO) {
        try {
            MerchantUserQuery query = toQuery(queryDTO, MerchantUserQuery.class);
            MerchantUserDO userDO = merchantManager.selectUser(query);
            MerchantUserDTO merchantUserDTO = toDTO(userDO, MerchantUserDTO.class);
            return ServiceResult.success(merchantUserDTO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    ServiceResult _saveUser(MerchantUserDO userDO) {
        try {
            merchantManager.saveUser(userDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
        return ServiceResult.success();
    }

    ServiceResult _saveShop(ShopDO shopDO) {
        return transactionTemplate.execute(status -> {
            try {
                int affectRow = merchantManager.saveShop(shopDO);
                if (affectRow < 1) {
                    return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????0?????????");
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                e.printStackTrace();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
            return ServiceResult.success();
        });
    }

    ServiceResult _saveShop(ShopDO shopDO, ShopAppDO shopAppDO) {
        ServiceResult  serviceResult = transactionTemplate.execute(status -> {
            try {
                int affectRow = merchantManager.saveShop(shopDO);
                if (affectRow < 1) {
                    return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "???????????????0?????????");
                }

                int save = shopAppManager.save(shopAppDO);
                if (save < 1) {
                    return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????app?????????0?????????");
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                e.printStackTrace();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
            return ServiceResult.success();
        });

        delShopAppCache(shopAppDO);
        return serviceResult;
    }

    public ShopAppDO getShopAppCache(@NotNull Long shopId, @NotEmpty String appId) throws Exception {
        String key = CacheKeyPrefix.APP_CONFIG_OF_SHOP;
        String field = shopId.toString() + CacheKeyPrefix.SEPARATOR + appId;
        ShopAppDO weixinConfig = cacheService.hGet(key, field, ShopAppDO.class);

        if (null == weixinConfig) {
            weixinConfig = selectShopAppWithValidate(shopId, appId);

            setShopAppCache(weixinConfig);
            // cacheService.hSet(key, field, weixinConfig, CacheExpire.DAYS_30);
        }

        return weixinConfig;
    }

    public Long getShopId(String appId) throws BusServiceException {
        String key = CacheKeyPrefix.APPID_MAP_SHOPID;
        Long shopId = cacheService.hGet(key, appId, Long.class);

        if (null == shopId) {
            ShopAppDO shopAppDO = new ShopAppDO();
            shopAppDO.setAppId(appId);
            List<ShopAppDO> shopAppDOS = null;
            try {
                shopAppDOS = shopAppManager.selectApps(shopAppDO);
            } catch (DaoManagerException e) {
                throw new BusServiceException("??????AppId???shop??????", e);
            }
            if (CollectionUtils.isEmpty(shopAppDOS)) {
                throw new BusServiceException("??????AppId???shop??????");
            }

            if (shopAppDOS.size() > 1) {
                throw new BusServiceException("AppId???shop??????????????????");
            }

            shopId = shopAppDOS.get(0).getShopId();

            cacheService.hSet(key, appId, shopId.toString());
        }

        return shopId;
    }

    void setShopAppCache(ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.APPID_MAP_SHOPID;
        cacheService.hSet(key, shopAppDO.getAppId(), shopAppDO.getShopId());

        key = CacheKeyPrefix.APP_CONFIG_OF_SHOP;
        cacheService.hSet(key, shopAppDO.getShopId() + CacheKeyPrefix.SEPARATOR + shopAppDO.getAppId(), shopAppDO);
    }
    void delShopAppCache(ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.APPID_MAP_SHOPID;
        cacheService.hDel(key, shopAppDO.getAppId());

        key = CacheKeyPrefix.APP_CONFIG_OF_SHOP;
        cacheService.hDel(key, shopAppDO.getShopId() + CacheKeyPrefix.SEPARATOR + shopAppDO.getAppId());
    }



    public ServiceResult<BigDecimal> selectShopFreightFree(Long shopId) {
        BigDecimal freightFree;
        try {
            freightFree = merchantManager.selectFreightFree(shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "??????????????????????????????");
        }
        return ServiceResult.success(freightFree);
    }

    public ServiceResult saveFreightFree(BigDecimal freightFree, Long shopId) {
        try {
            merchantManager.saveFreightFree(freightFree, shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success();
    }

    public ServiceResult<List<ShopDeliveryAreaDTO>> selectShopDeliveryArea(Long shopId) {
        List<ShopDeliveryAreaDTO> deliveryAreaDTOS = new ArrayList<>();
        try {
            String result = merchantManager.selectDeliveryArea(shopId);
            if (StringUtils.isNotBlank(result)) {
                deliveryAreaDTOS = JSONObject.parseArray(result, ShopDeliveryAreaDTO.class);
            }
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "??????????????????????????????");
        }
        return ServiceResult.success(deliveryAreaDTOS);
    }

    public ServiceResult saveDeliveryArea(String deliveryArea, Long shopId) {
        try {
            JSONObject.parseArray(deliveryArea, ShopDeliveryAreaDTO.class);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID);
        }
        try {
            merchantManager.saveDeliveryArea(deliveryArea, shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success();
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????app??????
     * @param shopId
     * @param authCode
     * @throws BusServiceException
     */
    public void saveWeixinAuth(@NotNull Long shopId, @NotNull Long operatorId, @NotEmpty String authCode,
            boolean fastRegiste) throws BusServiceException {
        // ?????????????????????????????????
        AuthorizationInfoDTO authorizationInfoDTO = componentService.queryAuth(authCode);

        // ??????app??????
        ShopAppDO shopAppDO = selectShopApp(shopId, authorizationInfoDTO.getAuthorizer_appid());
        if (null == shopAppDO) {
            shopAppDO = new ShopAppDO();
            shopAppDO.setAppId(authorizationInfoDTO.getAuthorizer_appid());
            String domain = authorizationInfoDTO.getAuthorizer_appid() + "." + ServiceManager.componentService
                    .getComponentConfig().getWebviewDomain()[ 0 ];
            shopAppDO.setDomain(domain);
            shopAppDO.setThirdType(0);
            shopAppDO.setShopId(shopId);
        }
        shopAppDO.setAuthRefreshToken(authorizationInfoDTO.getAuthorizer_refresh_token());
        shopAppDO.setAppSecret(null);
        shopAppDO.setOperatorId(operatorId);
        shopAppDO.setFastRegiste(fastRegiste ? 1 : 0);

        saveShopApp(shopAppDO);

        setShopAppCache(shopAppDO);

        InfoDTO infoDTO = new InfoDTO();
        infoDTO.setTitle("?????????????????????");
        infoDTO.setContent("appId:" + shopAppDO.getAppId());
        infoService.addInfo(infoDTO, shopId, true);

        AccountBasicInfoDTO accountBasicInfo;
        if (fastRegiste) {
            // ??????????????????????????? ??? cache
            accountBasicInfo = foundryMiniProgram4BasicInfoService.getAccountBasicInfo(shopAppDO, true);
        }
        else {
            accountBasicInfo = authorizerService.getAuthorizerInfo4MiniPrograme(shopAppDO, true);
        }

        final ShopAppDO shopAppDO1 = shopAppDO;
        scheduledExecutorService.schedule(() -> {
            // ????????????domain  webviewdoamin    ?????????????????????????????????????????????????????????????????????????????????
            String[] requestDomains = new String[] {"https://wmall.5jym.com"};
            try {
                foundryMiniProgram4BasicInfoService
                        .modifyDomain(FoundryMiniProgram4BasicInfoService.ModifyDomainType.set, shopAppDO1, requestDomains,
                                null, null, null);
            } catch (BusServiceException e) {
                // e.printStackTrace();
                log.warn("modifyDomain|{}", shopAppDO1.getAppId(), e);
            }

            if (accountBasicInfo != null && !Integer.valueOf(0).equals(accountBasicInfo.getPrincipal_type())) {
                String[] webviewDomains = new String[] {"https://" + shopAppDO1.getDomain()};
                try {
                    foundryMiniProgram4BasicInfoService
                            .setWebViewDomain(FoundryMiniProgram4BasicInfoService.ModifyDomainType.set, shopAppDO1,
                                    webviewDomains);
                } catch (BusServiceException e) {
                    // e.printStackTrace();
                    log.warn("setWebViewDomain|{}", shopAppDO1.getAppId(), e);
                }
            }

        }, 10, TimeUnit.SECONDS);
    }

    public void saveShopApp(ShopAppDO shopAppDO) throws BusServiceException {
        try {
            shopAppManager.save(shopAppDO);
        } catch (DaoManagerException e) {
            log.error("????????????app????????????{}", JSONObject.toJSONString(shopAppDO), e);
            throw new BusServiceException("????????????app????????????", e);
        }

        delShopAppCache(shopAppDO);
    }

    /**
     * ???????????????????????????????????????
     * @param shopId
     * @param appId
     * @throws BusServiceException
     */
    public void delShopApp(Long shopId, String appId) throws BusServiceException {
        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setAppId(appId);
        shopAppDO.setShopId(shopId);
        try {
            shopAppManager.del(shopAppDO);

            InfoDTO infoDTO = new InfoDTO();
            infoDTO.setTitle("???????????????????????????");
            infoDTO.setContent("appId:" + shopAppDO.getAppId());
            infoService.addInfo(infoDTO, shopId, true);
        } catch (DaoManagerException e) {
            throw new BusServiceException("????????????app????????????" + JSONObject.toJSONString(shopAppDO), e);
        }
    }

    public void savePayConfig(@NotNull Long shopAppDOId, @NotEmpty String appId, @NotNull Long shopId,
            @NotNull PayConfig payConfig) throws BusServiceException {
        if (!payConfig.isSub() && StringUtils.isBlank(payConfig.getApiKey())) {
            throw new BusServiceException("????????????????????????apiKey");
        }
        if (StringUtils.isBlank(payConfig.getMchId())) {
            throw new BusServiceException("???????????????");
        }

        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setId(shopAppDOId);
        shopAppDO.setShopId(shopId);
        shopAppDO.setAppId(appId);
        shopAppDO.setPayConfig(payConfig.toString());
        saveShopApp(shopAppDO);
    }

    public ServiceResult savePayType(@NotEmpty List<Integer> payTypes, @NotNull Long shopId) {
        try {
            merchantManager.savePayType(payTypes, shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success();
    }

    public ServiceResult saveDeliveryType(@NotEmpty List<Integer> deliveryTypes, @NotNull Long shopId) {
        try {
            merchantManager.saveDeliveryType(deliveryTypes, shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success();
    }

    public ServiceResult<List<Integer>> selectShopPayType(Long shopId) {
        String payTypes;
        try {
            payTypes = merchantManager.selectShopPayType(shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        List<Integer> payTypeList = ShopDO.parsePayType(payTypes);
        return ServiceResult.success(payTypeList);
    }

    public ServiceResult<byte[]> selectWXPayCert(Long shopId, String appId) {
        byte[] cert;
        try {
            cert = shopAppManager.selectWXPayCert(shopId, appId);
        } catch (Exception e) {
            log.error("selectWXPayCert|{}|{}", shopId, appId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success(cert);
    }

    public ServiceResult<Integer> saveWXPayCert(Long shopId, String appId, byte[] cert) {
        int count;
        try {
            count = shopAppManager.saveWXPayCert(shopId, appId, cert);
        } catch (Exception e) {
            log.error("saveWXPayCert|{}|{}", shopId, appId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success(count);
    }

    public ServiceResult<Boolean> existWXPayCert(Long shopId, String appId) {
        int count;
        try {
            count = shopAppManager.existWXPayCert(shopId, appId);
        } catch (Exception e) {
            log.error("existWXPayCert|{}|{}", shopId, appId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        return ServiceResult.success(count > 0);
    }

    public ServiceResult<List<Integer>> selectShopDeliveryType(Long shopId) {
        String payTypes;
        try {
            payTypes = merchantManager.selectShopDeliveryType(shopId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
        List<Integer> payTypeList = ShopDO.parsePayType(payTypes);
        return ServiceResult.success(payTypeList);
    }

    public ServiceResult<Boolean> existMerchantShop(MerchantShopQuery query) {
        int count = merchantManager.countMerchantShop(query);
        return ServiceResult.success(count > 0);
    }

    public ServiceResult<Boolean> existMerchant(MerchantUserQueryDTO queryDTO) {
        MerchantUserQuery query = toQuery(queryDTO, MerchantUserQuery.class);
        int count = merchantManager.countMerchant(query);
        return ServiceResult.success(count > 0);
    }

    public void saveStyleConfig(Long shopAppId, String appId, Long shopId, StyleConfig payConfig)
            throws BusServiceException {
        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setId(shopAppId);
        shopAppDO.setShopId(shopId);
        shopAppDO.setAppId(appId);
        shopAppDO.setStyle(payConfig.toString());
        try {
            shopAppManager.saveStyle(shopAppDO);
        } catch (DaoManagerException e) {
            log.error("????????????app????????????{}", JSONObject.toJSONString(shopAppDO), e);
            throw new BusServiceException("????????????app????????????", e);
        }
    }

    public String selectStyle(String appId, Long shopId) throws BusServiceException {
        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setShopId(shopId);
        shopAppDO.setAppId(appId);
        String style;
        try {
            style = shopAppManager.selectStyle(shopAppDO);
        } catch (DaoManagerException e) {
            log.error("????????????app????????????{}", JSONObject.toJSONString(shopAppDO), e);
            throw new BusServiceException("????????????app????????????", e);
        }

        if (StringUtils.isBlank(style)) {
            style = new StyleConfig().toString();
        }
        return style;
    }
}
