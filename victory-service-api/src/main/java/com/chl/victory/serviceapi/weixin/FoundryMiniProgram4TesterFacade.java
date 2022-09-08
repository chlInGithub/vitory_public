package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.GetTesterResult;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:05
 **/
public interface FoundryMiniProgram4TesterFacade {
    ServiceResult unbindTester(@NotNull Long shopId, @NotEmpty String appId, @NotEmpty String userstr);
    ServiceResult<GetTesterResult.Tester> bindTester(@NotNull Long shopId, @NotEmpty String appId, @NotEmpty String wechatid);
    ServiceResult<GetTesterResult> memberauth(@NotNull Long shopId, @NotEmpty String appId);
}
