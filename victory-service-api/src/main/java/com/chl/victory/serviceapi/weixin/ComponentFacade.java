package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;

/**
 * @author ChenHailong
 * @date 2020/8/24 18:56
 **/
public interface ComponentFacade {
    ServiceResult<String> getPreAuthUrl(@NotNull Long shopId);
}
