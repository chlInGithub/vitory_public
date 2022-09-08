package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SetNickNameDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:01
 **/
public interface FoundryMiniProgram4BasicInfoFacade {

    ServiceResult modifysignature(@NotNull Long shopId, @NotEmpty String appId, @NotNull String signature);

    /**
     * @param shopId
     * @param appId
     * @param imgId 图片服务器的imgId
     * @return
     */
    ServiceResult modifyheadimage(@NotNull Long shopId, @NotEmpty String appId, @NotNull String imgId);

    ServiceResult setNickName(@NotNull Long shopId, @NotEmpty String appId, @NotNull SetNickNameDTO setNickNameDTO);

    ServiceResult<AccountBasicInfoDTO> getAccountBasicInfo(@NotNull Long shopId, @NotEmpty String appId, boolean tryOther);

    ServiceResult<DomainResult> getDomain(@NotNull Long shopId, @NotEmpty String appId);

    void checkNickNameAuditResult();
}
