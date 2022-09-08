package com.chl.victory.web.controller.wm.weixin.pay;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.manager.RpcManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/p/wm/weixin/pay/notify/")
@Api(description = "微信支付notify处理类")
@Validated
@Slf4j
public class WeixinPayNotifyController {

    /**
     * 微信支付结果notify
     */
    @PostMapping("pay")
    @ApiOperation(value = "微信支付结果notify", produces = MediaType.TEXT_XML_VALUE)
    public String pay(@RequestBody String notify) {
        if (log.isInfoEnabled()) {
            log.info("payNotify|{}", notify);
        }
        String result = _pay(notify);

        if (log.isInfoEnabled()) {
            log.info("payNotify|{}|{}", notify, result);
        }

        return result;
    }

    private String _pay(String notify) {
        ServiceResult serviceResult = RpcManager.miniProgramFacade.payNotify(notify);
        if (serviceResult.getSuccess()) {
            return success();
        }
        return fail(serviceResult.getMsg());
    }

    /**
     * 微信支付退款结果notify
     */
    /*@PostMapping("refund")
    @ApiOperation(value = "微信支付退款结果notify", produces = MediaType.TEXT_XML_VALUE)
    public String refund(@RequestBody String notify) {
        // 本地解密成功，但是服务器解密失败，所以直接使用task查询退款结果。
        if (true) {
            return success();
        }

        if (log.isInfoEnabled()) {
            log.info("refundNotify|{}", notify);
        }

        String result = _refund(notify);

        if (log.isInfoEnabled()) {
            log.info("refundNotify|{}|{}", notify, result);
        }

        return result;
    }*/
    private String _refund(String notify) {
        ServiceResult serviceResult = RpcManager.miniProgramFacade.refundNotify(notify);
        if (serviceResult.getSuccess()) {
            return success();
        }
        return fail(serviceResult.getMsg());
    }

    String success() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    String success(String msg) {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[" + msg
                + "]]></return_msg></xml>";
    }

    String fail(String msg) {
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
    }

}
