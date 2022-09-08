package com.chl.victory.serviceimpl.weixin;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.InfoAndEventFacade;
import com.chl.victory.serviceapi.weixin.model.InfoAndEventDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.wxEventService;

/**
 * @author ChenHailong
 * @date 2020/9/3 13:53
 **/
@DubboService
public class InfoAndEventFacadeImpl implements InfoAndEventFacade {

    @Override
    public ServiceResult deal(InfoAndEventDTO infoAndEventDTO, String authorizerAppId) {
        try {
            String xml = wxEventService.getDecryptXMLMsg(infoAndEventDTO);
            wxEventService.deal(xml, authorizerAppId);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult deal(InfoAndEventDTO infoAndEventDTO) {
        try {
            String xml = wxEventService.getDecryptXMLMsg(infoAndEventDTO);
            wxEventService.deal(xml);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }
}
