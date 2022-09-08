package com.chl.victory.serviceimpl.weixin;

import java.util.List;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.MiniProgramFacade;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import com.chl.victory.serviceapi.weixin.model.WxPhoneDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.merchantService;
import static com.chl.victory.service.services.ServiceManager.payNotifyService;
import static com.chl.victory.service.services.ServiceManager.weixinMiniProgramService;

/**
 * @author ChenHailong
 * @date 2020/9/3 14:02
 **/
@DubboService
public class MiniProgramFacadeImpl implements MiniProgramFacade {

    @Override
    public ServiceResult payNotify(String notify) {
        return payNotifyService.payNotify(notify);
    }

    @Override
    public ServiceResult refundNotify(String notify) {
        return payNotifyService.refundNotify(notify);
    }

    @Override
    public ServiceResult<WeixinCode2Session> getCode2Session(Long shopId, String appId, String tCode) {
        WeixinCode2Session code2SessionFromWeixin;
        try {
            ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
            code2SessionFromWeixin = weixinMiniProgramService
                    .getCode2SessionFromWeixin(shopAppDO, tCode);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(code2SessionFromWeixin);
    }

    @Override
    public ServiceResult saveShopAndWeixinUser(Long shopId, String thirdId, String appId, String openid) {
        ServiceResult serviceResult;
        serviceResult = weixinMiniProgramService
                .saveShopAndWeixinUser(shopId, thirdId, appId, openid);
        return serviceResult;
    }

    @Override
    public ServiceResult<String> parseAndSaveMobile(WxPhoneDTO wxPhoneDTO) {
        ServiceResult<String> serviceResult= weixinMiniProgramService
                .parseAndSavePhone(wxPhoneDTO);
        return serviceResult;
    }

    @Override
    public void refreshAccessToken4Shops(List<Long> shopIds) {
        weixinMiniProgramService.refreshAccessToken4Shops(shopIds);
    }
}
