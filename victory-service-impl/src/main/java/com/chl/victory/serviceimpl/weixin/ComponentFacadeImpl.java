package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.ComponentFacade;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.componentService;

/**
 * @author ChenHailong
 * @date 2020/9/2 17:46
 **/
@DubboService
public class ComponentFacadeImpl implements ComponentFacade {

    @Override
    public ServiceResult<String> getPreAuthUrl(@NotNull Long shopId) {
        String preAuthCode = componentService.createPreAuthCode(shopId);
        String componentLoginPage = componentService.getComponentLoginPage(preAuthCode);
        return ServiceResult.success(componentLoginPage);
    }
}
