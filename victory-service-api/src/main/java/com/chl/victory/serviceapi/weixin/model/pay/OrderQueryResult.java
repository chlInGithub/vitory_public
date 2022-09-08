package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * 微信支付结果查询 返回结果的数据模型 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_2
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class OrderQueryResult extends PayBaseResult implements Serializable {

    String openid;

    /**
     * 交易状态
     * SUCCESS—支付成功
     * REFUND—转入退款
     * NOTPAY—未支付
     * CLOSED—已关闭
     * REVOKED—已撤销（刷卡支付）
     * USERPAYING--用户支付中
     * PAYERROR--支付失败(其他原因，如银行返回失败)
     */
    String trade_state;

    /**
     * 付款银行
     */
    String bank_type;

    /**
     * 订单总金额，单位为分
     */
    String total_fee;

    /**
     * 应结订单金额
     * 当订单使用了免充值型优惠券后返回该参数，应结订单金额=订单金额-免充值优惠券金额。
     */
    String settlement_total_fee;

    /**
     * 标价币种
     */
    String fee_type;

    /**
     * 微信支付订单号
     */
    String transaction_id;

    /**
     * 商户订单号
     */
    String out_trade_no;

    /**
     * 订单支付时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010
     */
    String time_end;

    /**
     * 交易状态描述
     */
    String trade_state_desc;

    public boolean isPaySuccess() {
        return "SUCCESS".equals(trade_state);
    }
}
