package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/12 16:37
 * 微信预支付 参数 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1&index=1
 **/
@Data
public class PayUnifiedOrderParam extends PayBaseParam implements Serializable {

    /**
     * 商品简单描述
     */
    String body;

    /**
     * 订单总金额，单位为分
     */
    Integer total_fee;

    /**
     * 支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
     */
    String spbill_create_ip;

    /**
     * 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
     */
    String notify_url;

    /**
     * 小程序取值如下：JSAPI
     */
    String trade_type;

    String sign_type;

    /**
     * trade_type=JSAPI时（即JSAPI支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识
     */
    String openid;

    /**
     * 用户子标识 openid和sub_openid可以选传其中之一，如果选择传sub_openid,则必须传sub_appid。
     */
    String sub_openid;
}
