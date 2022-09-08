package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.Data;

import static com.chl.victory.service.services.ServiceManager.componentService;

/**
 * 授权成功
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/api/authorize_event.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXEventHandler4Authorized implements WXEventHandler {

    @Override
    public void hadler(String xml) throws BusServiceException {
        WXEventDTO wxEventDTO = XmlUtil.fromXml(xml,  WXEventDTO.class);

        // 预授权码-shopId
        Long shopId = componentService.getPreAuthCodeCache(wxEventDTO.getPreAuthCode());
        if (null == shopId) {
            throw new BusServiceException("预授权码和店铺关系不存在");
        }

        ServiceManager.merchantService.saveWeixinAuth(shopId, ShopConstants.DEFAULT_OPERATOER, wxEventDTO.getAuthorizationCode(), false);
    }

    @Data
    private static class WXEventDTO{

        /**
         * 第三方平台 appid
         */
        String AppId;

        /**
         * 时间戳
         */
        Long CreateTime;

        /**
         * 通知类型
         */
        String InfoType;

        /**
         * 公众号或小程序的 appid
         */
        String AuthorizerAppid;

        /**
         * 授权码
         */
        String AuthorizationCode;

        /**
         * 授权码过期时间 单位秒
         */
        String AuthorizationCodeExpiredTime;

        /**
         * 预授权码
         */
        String PreAuthCode;
    }
}
