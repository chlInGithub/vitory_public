package com.chl.victory.serviceapi.weixin;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.InfoAndEventDTO;

/**
 * @author ChenHailong
 * @date 2020/8/25 15:21
 **/
public interface InfoAndEventFacade {

    ServiceResult deal(InfoAndEventDTO infoAndEventDTO, String authorizerAppId);

    ServiceResult deal(InfoAndEventDTO infoAndEventDTO);
}
