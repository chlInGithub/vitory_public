package com.chl.victory.web.controller.wm.weixin.thirdplatform;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.serviceapi.weixin.model.InfoAndEventDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ChenHailong
 * @date 2020/5/28 14:02
 **/
@Controller
@RequestMapping("/p/wm/weixin/thirdplatform/notify/")
@Slf4j
public class NotifyController {

    /*String getAppId(String xml) throws AesException {
        Object[] extract = XMLParse.extract(xml);

        if (extract[ 2 ] == null) {
            return null;
        }

        String appId = extract[ 2 ].toString();

        return appId;
    }*/

    /**
     * 授权事件notify
     * 用于接收取消授权通知、授权成功通知、授权更新通知，也用于接收验证票据（component_verify_ticket），component_verify_ticket 是验证平台方的重要凭据，请妥善保存。
     */
    @PostMapping(path = "authEvent", produces = MediaType.TEXT_XML_VALUE)
    @ResponseBody
    public String authEvent(@RequestParam("msg_signature") @NotEmpty String msgSignature,
            @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
            @RequestBody String postdata, HttpServletRequest request, HttpServletResponse response) {

        if (log.isInfoEnabled()) {
            log.info("authEvent|{}|{}|{}|{}", msgSignature, timestamp, nonce, postdata);
        }

        try {
            InfoAndEventDTO infoAndEventDTO = new InfoAndEventDTO();
            infoAndEventDTO.setMsgSignature(msgSignature);
            infoAndEventDTO.setNonce(nonce);
            infoAndEventDTO.setPostdata(postdata);
            infoAndEventDTO.setTimestamp(timestamp);
            RpcManager.infoAndEventFacade.deal(infoAndEventDTO);
            return "success";
        } catch (Exception e) {
            log.error("authEvent|{}|{}|{}|{}", msgSignature, timestamp, nonce, postdata, e);
        }

        return "fail";
    }

    /**
     * 消息与事件notify
     * 已授权公众号、小程序的消息和事件
     */
    @PostMapping(path = "{appId}/infoAndEvent")
    @ResponseBody
    public String infoAndEvent(@PathVariable String appId, @RequestParam("msg_signature") @NotEmpty String msgSignature,
            @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
            @RequestBody String postdata, HttpServletResponse response) {

        if (log.isInfoEnabled()) {
            log.info("infoAndEvent|{}|{}|{}|{}|{}", appId, msgSignature, timestamp, nonce, postdata);
        }

        try {
            InfoAndEventDTO infoAndEventDTO = new InfoAndEventDTO();
            infoAndEventDTO.setMsgSignature(msgSignature);
            infoAndEventDTO.setNonce(nonce);
            infoAndEventDTO.setPostdata(postdata);
            infoAndEventDTO.setTimestamp(timestamp);
            RpcManager.infoAndEventFacade.deal(infoAndEventDTO, appId);
            return "success";
        } catch (Exception e) {
            log.error("infoAndEvent|{}|{}|{}|{}|{}", appId, msgSignature, timestamp, nonce, postdata, e);
        }
        return "fail";
    }

}
