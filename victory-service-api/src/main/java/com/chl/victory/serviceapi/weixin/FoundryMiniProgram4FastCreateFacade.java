package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate.FastRegisterDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 18:50
 **/
public interface FoundryMiniProgram4FastCreateFacade {
    ServiceResult fastRegister(@NotNull Long shopId, @NotNull FastRegisterDTO model);
}
