package com.chl.victory.serviceapi.fee.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class OrderFeeDTO extends BaseDTO implements Serializable {
    /**
     * 用户ID
     * user_id
     */
    private Long userId;

    /**
     * 佣金归属年月 yyyyMM
     * statistic_date
     */
    private Integer statisticDate;

    /**
     * 订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 子订单ID
     * sub_id
     */
    private Long subId;

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 佣金
     * fee
     */
    private BigDecimal fee;

    /**
     * 佣金规则快照
     * rule
     */
    private String rule;

    /**
     * 1：待审核，2：待支付，3：已支付
     * status
     */
    private Byte status;

}