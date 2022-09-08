package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4BasicInfoService;
import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4BasicInfoService;

/**
 * 名称审核
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/wxa_nickname_audit.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXInfoHandler4WxaNicknameAudit implements WXInfoHandler {

    @Override
    public void hadler(String xml, ShopAppDO shopAppDO) throws BusServiceException {
        WXInfoDTO wxEventDTO = XmlUtil.fromXml(xml,  WXInfoDTO.class);

        FoundryMiniProgram4BasicInfoService.SetNickNameResultTemp nickNameAuditingCache = foundryMiniProgram4BasicInfoService
                .getNickNameAuditingCache(shopAppDO);
        if (nickNameAuditingCache == null) {
            throw new BusServiceException("不存在审核cache");
        }

        if (!nickNameAuditingCache.getNickName().equals(wxEventDTO.getNickname())) {
            throw new BusServiceException("与审核cache不相同|" + nickNameAuditingCache.getNickName());
        }

        Integer ret = wxEventDTO.getRet();
        if (2 == ret) {
            foundryMiniProgram4BasicInfoService.nickNameAuditFail(shopAppDO, wxEventDTO.getReason());
            return;
        }

        if (3 == ret) {
            foundryMiniProgram4BasicInfoService.nickNameAuditSuccess(shopAppDO, wxEventDTO.getNickname());
            return;
        }

    }
    @Data
    private  static  class WXInfoDTO {

        String ToUserName;
        String FromUserName;

        /**
         * 时间戳
         */
        Long CreateTime;
        Integer ret;
        String nickname;
        String reason;
    }
}
