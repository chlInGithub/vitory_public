package com.chl.victory.serviceapi.weixin;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.pay.WxPrePayDTO;

/**
 * @author ChenHailong
 * @date 2020/8/31 14:12
 **/
public interface WxPayFacade {

    ServiceResult<Boolean> checkPayed(Long shopId, Long userId, String appId, Long orderId);

    ServiceResult<WxPrePayDTO> prePay(Long shopId, Long userId, String appId, Long orderId);
}
