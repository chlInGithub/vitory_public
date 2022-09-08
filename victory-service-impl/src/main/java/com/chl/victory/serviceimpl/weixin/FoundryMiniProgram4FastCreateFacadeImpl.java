package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4FastCreateFacade;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate.FastRegisterDTO;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author ChenHailong
 * @date 2020/9/3 13:39
 **/
@DubboService
public class FoundryMiniProgram4FastCreateFacadeImpl implements FoundryMiniProgram4FastCreateFacade {

    @Override
    public ServiceResult fastRegister(@NotNull Long shopId, @NotNull FastRegisterDTO model) {
        try {
            ServiceManager.foundryMiniProgram4FastCreateService.fastRegister(shopId, model);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }
}
