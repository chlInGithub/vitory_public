package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.AuditResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.CommitDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.ReleaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.SubmitAuditDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:03
 **/
public interface FoundryMiniProgram4CodeFacade {

    ServiceResult<byte[]> getQRCode4Test(@NotNull Long shopId, @NotEmpty String appId);

    ServiceResult<AuditResult> submitAudit(@NotNull Long shopId, @NotEmpty String appId,
            @NotNull SubmitAuditDTO submitAuditDTO);

    ServiceResult<ReleaseResult> release(@NotNull Long shopId, @NotEmpty String appId);

    ServiceResult revertcoderelease(@NotNull Long shopId, @NotEmpty String appId);

    ServiceResult undocodeaudit(@NotNull Long shopId, @NotEmpty String appId);

    ServiceResult commit(@NotNull Long shopId, @NotEmpty String appId, @NotNull CommitDTO commitDTO);

    ServiceResult<AuditResult> getLastAudit(@NotNull Long shopId, @NotEmpty String appId);

    void checkAuditResult();
}
