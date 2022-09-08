package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.AuthorizerFacade;
import com.chl.victory.serviceapi.weixin.enums.WeixinMediaTypeEnum;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.UploadedMediaDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.authorizerService;

/**
 * @author ChenHailong
 * @date 2020/9/2 17:31
 **/
@DubboService
public class AuthorizerFacadeImpl implements AuthorizerFacade {

    @Override
    public void setMediaCache(@NotNull Long shopId, @NotEmpty String appId, @NotNull UploadedMediaDTO mediaDTO) {
        authorizerService.setMediaCache(shopId, appId, mediaDTO);
    }

    @Override
    public ServiceResult<String> getUploadMediaUrl(@NotNull Long shopId, @NotEmpty String appId, WeixinMediaTypeEnum weixinMediaTypeEnum) {
        try {
            return ServiceResult.success(authorizerService.getUploadMediaUrl(shopId, appId, weixinMediaTypeEnum));
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }
}
