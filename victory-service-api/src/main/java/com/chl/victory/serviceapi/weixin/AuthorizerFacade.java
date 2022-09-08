package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.enums.WeixinMediaTypeEnum;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.UploadedMediaDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:15
 **/
public interface AuthorizerFacade {

    void setMediaCache(@NotNull Long shopId, @NotEmpty String appId, @NotNull UploadedMediaDTO mediaDTO);

    ServiceResult<String> getUploadMediaUrl(@NotNull Long shopId, @NotEmpty String appId, WeixinMediaTypeEnum weixinMediaTypeEnum)
            throws BusServiceException;

}
