package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * 微信支付 退款 返回结果的数据模型 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_4
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class RefundResult extends PayBaseResult implements Serializable {

    /**
     * 商户订单号
     */
    String out_trade_no;

    /**
     * 商户退款单号
     */
    String out_refund_no;

    /**
     * 微信退款单号
     */
    String refund_id;

    /**
     * 退款总金额,单位为分,可以做部分退款
     */
    Integer refund_fee;

    /**
     * 去掉非充值代金券退款金额后的退款金额，退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
     */
    Integer settlement_refund_fee;

    /**
     * 订单总金额，单位为分，只能为整数
     */
    Integer total_fee;

    /**
     * 去掉非充值代金券金额后的订单总金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
     */
    Integer settlement_total_fee;
}
