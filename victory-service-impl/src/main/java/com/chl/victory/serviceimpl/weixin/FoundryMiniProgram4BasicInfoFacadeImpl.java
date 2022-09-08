package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4BasicInfoFacade;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainInfo;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SetNickNameDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.WebViewDomainInfo;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4BasicInfoService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/9/2 17:51
 **/
@DubboService
public class FoundryMiniProgram4BasicInfoFacadeImpl implements FoundryMiniProgram4BasicInfoFacade {

    @Override
    public ServiceResult modifysignature(@NotNull Long shopId, @NotEmpty String appId, @NotNull String signature) {
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4BasicInfoService.modifysignature(weixinConfig, signature);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult modifyheadimage(@NotNull Long shopId, @NotEmpty String appId, @NotNull String imgId) {
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            String mediaId = ServiceManager.authorizerService.getMediaCache(shopId, appId, imgId);
            foundryMiniProgram4BasicInfoService.modifyheadimage(weixinConfig, mediaId);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult setNickName(@NotNull Long shopId, @NotEmpty String appId,
            @NotNull SetNickNameDTO setNickNameDTO) {
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4BasicInfoService.setNickName(weixinConfig, setNickNameDTO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult<AccountBasicInfoDTO> getAccountBasicInfo(@NotNull Long shopId, @NotEmpty String appId,
            boolean tryOther) {
        AccountBasicInfoDTO accountBasicInfo;
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            accountBasicInfo = foundryMiniProgram4BasicInfoService.getAccountBasicInfo(weixinConfig, tryOther);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(accountBasicInfo);
    }

    @Override
    public ServiceResult<DomainResult> getDomain(@NotNull Long shopId, @NotEmpty String appId) {
        DomainInfo domain;
        WebViewDomainInfo webViewDomain;
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            domain = foundryMiniProgram4BasicInfoService.getDomain(weixinConfig);
            webViewDomain = foundryMiniProgram4BasicInfoService.getWebViewDomain(weixinConfig);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        DomainResult domainResult = new DomainResult();
        domainResult.setDomainInfo(domain);
        domainResult.setWebViewDomainInfo(webViewDomain);
        return ServiceResult.success(domainResult);
    }

    @Override
    public void checkNickNameAuditResult() {
        foundryMiniProgram4BasicInfoService.checkNickAudit();
    }
}
