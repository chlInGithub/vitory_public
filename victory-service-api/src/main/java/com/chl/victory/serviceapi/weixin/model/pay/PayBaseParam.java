package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/12 16:44
 **/
@Data
public class PayBaseParam implements Serializable {
    /**
     * 微信分配的小程序ID
     */
    String appid;

    /**
     * 微信支付分配的商户号
     */
    String mch_id;

    /**
     * 子商户公众账号ID
     */
    String sub_appid;

    /**
     * 子商户号
     */
    String sub_mch_id;

    /**
     * 随机字符串，长度要求在32位以内。
     */
    String nonce_str;

    /**
     * 通过签名算法计算得出的签名值
     */
    String sign;

    /**
     * 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*且在同一个商户号下唯一。
     */
    String out_trade_no;
}
