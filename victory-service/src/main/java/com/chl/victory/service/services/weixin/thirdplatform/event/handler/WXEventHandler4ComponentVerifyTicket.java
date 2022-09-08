package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import java.util.Date;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 第三方平台 验证票据
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/api/component_verify_ticket.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXEventHandler4ComponentVerifyTicket implements WXEventHandler {

    @Override
    public void hadler(String xml) throws BusServiceException {
        WXEventDTO wxEventDTO = XmlUtil.fromXml(xml, WXEventDTO.class);

        // 验证创建时间是否超过5分钟
        Date date = new Date(wxEventDTO.getCreateTime() * 1000);
        Date now = new Date();
        Date before5Mins = DateUtils.addMinutes(now, -5);
        String s1 = DateFormatUtils.format(date, DateConstants.format1);
        String s2 = DateFormatUtils.format(before5Mins, DateConstants.format1);
        if (date.before(before5Mins)) {
            throw new BusServiceException("创建时间距当前时间已超过5分钟" + s1 + " " + s2 + " " + wxEventDTO.toString());
        }

        ServiceManager.componentService
                .updateComponentVerifyTicket(wxEventDTO.getAppId(), wxEventDTO.getComponentVerifyTicket());

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

        String ComponentVerifyTicket;
    }
}
