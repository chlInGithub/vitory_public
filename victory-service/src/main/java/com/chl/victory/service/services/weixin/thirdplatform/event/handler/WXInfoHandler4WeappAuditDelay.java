package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.Data;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4CodeService;

/**
 * 代码审核
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/code/audit_event.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXInfoHandler4WeappAuditDelay implements WXInfoHandler {

    @Override
    public void hadler(String xml, ShopAppDO shopAppDO) throws BusServiceException {
        WXInfoDTO wxEventDTO = XmlUtil.fromXml(xml,  WXInfoDTO.class);

        foundryMiniProgram4CodeService.modifyAuditCache(shopAppDO, 2, wxEventDTO.getReason());
    }
    @Data
    private  static  class WXInfoDTO {

        String ToUserName;
        String FromUserName;

        /**
         * 时间戳
         */
        Long CreateTime;
        String Reason;
        Long DelayTime;
        String ScreenShot;
    }
}
