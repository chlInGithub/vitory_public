package com.chl.victory.serviceimpl.weixin;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.WxPayFacade;
import com.chl.victory.serviceapi.weixin.model.pay.WxPrePayDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.wxPayService;

/**
 * @author ChenHailong
 * @date 2020/9/3 14:22
 **/
@DubboService
public class WxPayFacadeImpl implements WxPayFacade {

    @Override
    public ServiceResult<Boolean> checkPayed(Long shopId, Long userId, String appId, Long orderId) {
        try {
            ServiceResult<Boolean> booleanServiceResult = wxPayService
                    .checkPayed(shopId, userId, appId, orderId);
            return booleanServiceResult;
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }

    }

    @Override
    public ServiceResult<WxPrePayDTO> prePay(Long shopId, Long userId, String appId, Long orderId) {
        try {
            return wxPayService.prePay(shopId, userId, appId, orderId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }
}
