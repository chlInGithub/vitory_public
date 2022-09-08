package com.chl.victory.serviceapi.weixin;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import com.chl.victory.serviceapi.weixin.model.WxPhoneDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 18:37
 **/
public interface MiniProgramFacade {
    ServiceResult payNotify(String notify);
    ServiceResult refundNotify(String notify);

    ServiceResult<WeixinCode2Session> getCode2Session(Long shopId, String appId, String tCode);

    ServiceResult saveShopAndWeixinUser(Long shopId, String thirdId, String appid, String openid);

    ServiceResult<String> parseAndSaveMobile(WxPhoneDTO wxPhoneDTO);

    void refreshAccessToken4Shops(List<Long> shopIds);
}
