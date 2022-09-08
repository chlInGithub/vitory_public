package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.Data;

import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * 取消授权
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXEventHandler4UnAuthorized implements WXEventHandler {

    @Override
    public void hadler(String xml) throws BusServiceException {
        WXEventDTO wxEventDTO = XmlUtil.fromXml(xml, WXEventDTO.class);

        Long shopId = merchantService.getShopId(wxEventDTO.getAuthorizerAppid());
        if (null == shopId) {
            throw new BusServiceException("appId和店铺关系不存在" + wxEventDTO.getAuthorizerAppid());
        }

        merchantService.delShopApp(shopId, wxEventDTO.getAuthorizerAppid());
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
    }
}
