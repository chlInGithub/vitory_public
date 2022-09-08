package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4TesterFacade;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.GetTesterResult;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/9/3 13:46
 **/
@DubboService
public class FoundryMiniProgram4TesterFacadeImpl implements FoundryMiniProgram4TesterFacade {

    @Override
    public ServiceResult unbindTester(@NotNull Long shopId, @NotEmpty String appId, @NotEmpty String userstr) {
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            ServiceManager.foundryMiniProgram4TesterService.unbindTester(shopAppDO, userstr);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult<GetTesterResult.Tester> bindTester(@NotNull Long shopId, @NotEmpty String appId, @NotEmpty String wechatid) {
        GetTesterResult.Tester tester;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            tester = ServiceManager.foundryMiniProgram4TesterService
                    .bindTester(shopAppDO, wechatid);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(tester);
    }

    @Override
    public ServiceResult<GetTesterResult> memberauth(@NotNull Long shopId, @NotEmpty String appId) {
        GetTesterResult memberauth;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            memberauth = ServiceManager.foundryMiniProgram4TesterService.memberauth(shopAppDO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(memberauth);
    }
}
