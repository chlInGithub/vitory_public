package com.chl.victory.service.services.weixin.thirdplatform.event;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.services.weixin.thirdplatform.ComponentService;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler4Authorized;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler4ComponentVerifyTicket;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler4Fasteregister;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler4UnAuthorized;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXEventHandler4UpdateAuthorized;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXInfoHandler;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXInfoHandler4WeappAuditDelay;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXInfoHandler4WeappAuditFail;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXInfoHandler4WeappAuditSuccess;
import com.chl.victory.service.services.weixin.thirdplatform.event.handler.WXInfoHandler4WxaNicknameAudit;
import com.chl.victory.service.weixinsdk.aes.AesException;
import com.chl.victory.service.weixinsdk.aes.WXBizMsgCrypt;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.model.InfoAndEventDTO;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/6/8 9:29
 **/
@Service
@Slf4j
public class WXEventService {

    /**
     * 微信服务器发送给第三方平台自身的通知或事件推送（如授权通知，component_verify_ticket 推送, 新建小程序结果等）。
     */
    private Map<InfoTypeEnum, WXEventHandler> infoTypeEventHandlerMap;

    /**
     * 发送给公众号/小程序的消息（由第三方平台代收）,如名称审核、代码审核等
     */
    private Map<EventEnum, WXInfoHandler> eventHandlerMap;

    @PostConstruct
    public void postConstruct() {
        infoTypeEventHandlerMap = new HashMap<>();
        infoTypeEventHandlerMap.put(InfoTypeEnum.authorized, new WXEventHandler4Authorized());
        infoTypeEventHandlerMap.put(InfoTypeEnum.unauthorized, new WXEventHandler4UnAuthorized());
        infoTypeEventHandlerMap.put(InfoTypeEnum.updateauthorized, new WXEventHandler4UpdateAuthorized());
        infoTypeEventHandlerMap.put(InfoTypeEnum.component_verify_ticket, new WXEventHandler4ComponentVerifyTicket());
        infoTypeEventHandlerMap.put(InfoTypeEnum.notify_third_fasteregister, new WXEventHandler4Fasteregister());

        eventHandlerMap = new HashMap<>();
        eventHandlerMap.put(EventEnum.wxa_nickname_audit, new WXInfoHandler4WxaNicknameAudit());
        eventHandlerMap.put(EventEnum.weapp_audit_success, new WXInfoHandler4WeappAuditSuccess());
        eventHandlerMap.put(EventEnum.weapp_audit_fail, new WXInfoHandler4WeappAuditFail());
        eventHandlerMap.put(EventEnum.weapp_audit_delay, new WXInfoHandler4WeappAuditDelay());
    }

    /**
     * 处理事件
     */
    public void deal(@NotEmpty String xml) {
        try {
            WXEventDTO wxEventDTO = XmlUtil.fromXml(xml,  WXEventDTO.class);
            if (wxEventDTO != null && StringUtils.isNotBlank(wxEventDTO.getAppId())) {
                dealEvent(xml, wxEventDTO);
                return;
            }
        } catch (Exception e) {
            log.error("dealEx|{}", xml, e);
        }
    }
    /**
     * 处理消息
     * @param authorizerAppId 授权者APPId
     */
    public void deal(@NotEmpty String xml, String authorizerAppId) {
        try {
            WXInfoDTO wxInfoDTO =  XmlUtil.fromXml(xml,  WXInfoDTO.class);
            if (wxInfoDTO != null && StringUtils.isNotBlank(wxInfoDTO.getToUserName())) {
                dealInfo(xml, wxInfoDTO, authorizerAppId);
            }
        } catch (Exception e) {
            log.error("dealEx|{}", xml, e);
        }
    }

    private void dealInfo(String content, WXInfoDTO wxInfoDTO, String authorizerAppId) throws BusServiceException {
        String appId = authorizerAppId;
        Long shopId = merchantService.getShopId(appId);
        // 验证shop app关系
        ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);

        WXInfoHandler wxEventHandler = eventHandlerMap.get(wxInfoDTO.getEvent());
        if (null == wxEventHandler) {
            log.error("没有相关的info处理器{}", content);
            throw new BusServiceException("没有相关的info处理器");
        }

        wxEventHandler.hadler(content, shopAppDO);
    }

    void dealEvent(String content, WXEventDTO wxEventDTO) throws BusServiceException {
        ServiceManager.componentService.assertComponentId(wxEventDTO.getAppId());

        WXEventHandler wxEventHandler = infoTypeEventHandlerMap.get(wxEventDTO.getInfoType());
        if (null == wxEventHandler) {
            log.error("没有相关的event处理器{}", content);
            throw new BusServiceException("没有相关的event处理器");
        }

        wxEventHandler.hadler(content);
    }

    /**
     * 消息 事件 报文解密
     */
    public String getDecryptXMLMsg(InfoAndEventDTO infoAndEventDTO) throws AesException {
        ComponentService.ComponentConfig componentConfig = ServiceManager.componentService.getComponentConfig();
        // 解密
        WXBizMsgCrypt pc = new WXBizMsgCrypt(componentConfig.getToken(), componentConfig.getEncodingAesKey(),
                componentConfig.getComponentAppId());
        String xml = pc.decryptXMLMsg(infoAndEventDTO.getMsgSignature(), infoAndEventDTO.getTimestamp(), infoAndEventDTO.getNonce(), infoAndEventDTO.getPostdata());
        return xml;
    }

    @Data
    private  static  class WXInfoDTO {

        String ToUserName;
        String FromUserName;

        /**
         * 时间戳
         */
        Long CreateTime;

        MsgTypeEnum MsgType;

        EventEnum Event;

        public void setMsgType(String msgType) {
            MsgType = MsgTypeEnum.valueOf(msgType);
        }

        public void setEvent(String event) {
            Event = EventEnum.valueOf(event);
        }
    }

    @Data
    private    static  class WXEventDTO {

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
        InfoTypeEnum InfoType;

        public void setInfoType(String infoType) {
            InfoType = InfoTypeEnum.valueOf(infoType);
        }

        public static void main(String[] args) {
            String content =
                    "<xml><AppId><![CDATA[wx2dcbe9091987e73b]]></AppId>\n" + "<CreateTime>1592978881</CreateTime>\n"
                            + "<InfoType><![CDATA[notify_third_fasteregister]]></InfoType><status>0</status>\n"
                            + "<msg><![CDATA[OK]]></msg>\n" + "<appid><![CDATA[wxb83bf939db53ccb7]]></appid>\n"
                            + "<auth_code><![CDATA[queryauthcode@@@VTlA_R8pzyTq4LhOkyRcGGQhLUYzf7v6wB1LsoqGhnK-X74yF-TFzlaQBrHE6pDx8GWigVWZyHs5Ze6P2Rsahw]]></auth_code>\n"
                            + "<info>\n" + "<name><![CDATA[香河博予科技有限公司]]></name>\n"
                            + "<code><![CDATA[91131024MA0D4UD80L]]></code>\n" + "<code_type>1</code_type>\n"
                            + "<legal_persona_wechat><![CDATA[HenrySusie]]></legal_persona_wechat>\n"
                            + "<legal_persona_name><![CDATA[陈海龙]]></legal_persona_name>\n"
                            + "<component_phone><![CDATA[18500425785]]></component_phone>\n" + "</info>\n" + "</xml>";
            WXEventDTO wxEventDTO = XmlUtil.fromXml(content, "xml", WXEventDTO.class);
            System.out.println();
        }
    }

    /**
     * 通知类型
     */
    enum InfoTypeEnum {
        /**
         * 取消授权
         */
        unauthorized
        /**
         * 更新授权
         */
        , updateauthorized
        /**
         * 授权成功
         */
        , authorized
        /**
         * 第三平台验证票据
         */
        , component_verify_ticket
        , notify_third_fasteregister;
    }
    enum MsgTypeEnum {
        event;
    }
    public enum EventEnum {
        weapp_audit_success
        ,weapp_audit_fail
        ,weapp_audit_delay
        ,wxa_nickname_audit
        ;
    }
}
