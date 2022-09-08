package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 微信退款结果查询 返回结果的数据模型 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=9_5
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class RefundQueryResult extends PayBaseResult implements Serializable {
    /**
     * 微信订单号
     */
    @NotEmpty String transaction_id;

    /**
     * 商户订单号
     */
    @NotEmpty String out_trade_no;

    /**
     * 订单总金额，单位为分
     */
    @NotNull Integer total_fee;

    /**
     * 当该订单有使用非充值券时，返回此字段。应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
     */
    Integer settlement_total_fee;

    Integer refund_count;

    /**
     * 微信退款单号
     */
    @NotEmpty String refund_id_0;

    /**
     * 商户退款单号
     */
    @NotEmpty String out_refund_no_0;

    /**
     * 申请退款金额
     */
    @NotNull Integer refund_fee_0;

    /**
     * 退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
     */
    @NotNull Integer settlement_refund_fee_0;

    /**
     * SUCCESS-退款成功
     * CHANGE-退款异常
     * REFUNDCLOSE—退款关闭
     */
    @NotNull String refund_status_0;

    /**
     * 资金退款至用户帐号的时间，格式2017-12-15 09:46:01
     */
    String refund_success_time_0;

    /**
     * 退款入账账户
     */
    @NotNull String refund_recv_accout_0;

    /**
     * 退款资金来源
     */
    @NotNull String refund_account_0;

    public boolean isRefunded() {
        return "SUCCESS".equals(refund_status_0);
    }
    public boolean isClosed() {
        return "REFUNDCLOSE".equals(refund_status_0);
    }
    public boolean isProcessing() {
        return "PROCESSING".equals(refund_status_0);
    }
    public boolean isChange() {
        return "CHANGE".equals(refund_status_0);
    }
}
