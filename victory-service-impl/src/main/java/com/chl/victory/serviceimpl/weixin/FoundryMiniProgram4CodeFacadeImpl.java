package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4CodeFacade;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.AuditResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.CommitDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.ReleaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.SubmitAuditDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.GetTesterResult;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4CodeService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/9/3 11:12
 **/
@DubboService
public class FoundryMiniProgram4CodeFacadeImpl implements FoundryMiniProgram4CodeFacade {

    @Override
    public ServiceResult<byte[]> getQRCode4Test(@NotNull Long shopId, @NotEmpty String appId) {
        byte[] qrCode4Test;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            qrCode4Test = foundryMiniProgram4CodeService.getQRCode4Test(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(qrCode4Test);
    }

    @Override
    public ServiceResult<AuditResult> submitAudit(@NotNull Long shopId, @NotEmpty String appId, @NotNull SubmitAuditDTO submitAuditDTO) {
        AuditResult auditResult;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            auditResult = foundryMiniProgram4CodeService.submitAudit(shopAppDO, submitAuditDTO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(auditResult);
    }

    @Override
    public ServiceResult<AuditResult> getLastAudit(@NotNull Long shopId, @NotEmpty String appId) {
        AuditResult auditResult;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            auditResult = foundryMiniProgram4CodeService.getLatestAuditstatus(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(auditResult);
    }

    @Override
    public void checkAuditResult() {
        foundryMiniProgram4CodeService.checkAuditResult();
    }

    @Override
    public ServiceResult<ReleaseResult> release(@NotNull Long shopId, @NotEmpty String appId) {
        ReleaseResult release;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            release = foundryMiniProgram4CodeService.release(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(release);
    }

    @Override
    public ServiceResult revertcoderelease(@NotNull Long shopId, @NotEmpty String appId) {
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4CodeService.revertcoderelease(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();

    }

    @Override
    public ServiceResult undocodeaudit(@NotNull Long shopId, @NotEmpty String appId) {
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4CodeService.undocodeaudit(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult commit(@NotNull Long shopId, @NotEmpty String appId, @NotNull CommitDTO commitDTO) {
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4CodeService.commit(shopAppDO, commitDTO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }
}
