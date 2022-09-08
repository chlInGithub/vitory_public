package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4CodeService;

/**
 * 代码审核
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/code/audit_event.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXInfoHandler4WeappAuditSuccess implements WXInfoHandler {

    @Override
    public void hadler(String content, ShopAppDO shopAppDO) throws BusServiceException {
        foundryMiniProgram4CodeService.modifyAuditCache(shopAppDO, 0, null);
    }
}
