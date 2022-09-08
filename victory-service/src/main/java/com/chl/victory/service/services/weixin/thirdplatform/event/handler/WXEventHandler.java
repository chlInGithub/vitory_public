package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.serviceapi.exception.BusServiceException;

/**
 * @author ChenHailong
 * @date 2020/6/8 9:48
 **/
public interface WXEventHandler {

    void hadler(String xml) throws BusServiceException;
}
