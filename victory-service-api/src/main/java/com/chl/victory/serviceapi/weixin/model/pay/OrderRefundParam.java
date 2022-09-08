package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/12 16:37
 * 微信支付订单退款 参数 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_4
 **/
@Data
public class OrderRefundParam extends PayBaseParam implements Serializable {

    /**
     * 商户退款单号
     */
    String out_refund_no;

    /**
     * 订单总金额，单位为分，只能为整数
     */
    Integer total_fee;

    /**
     * 退款总金额，订单总金额，单位为分，只能为整数
     */
    Integer refund_fee;

    /**
     * 异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数
     * 如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。
     */
    String notify_url;
}
