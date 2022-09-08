package com.chl.victory.dao.query.order;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hailongchen9
 */
@Data
public class RefundOrderQuery extends BaseQuery {
    /**
     * 主订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 退款类型，1退货退款  2仅退款   3换货
     * type
     */
    private Byte type;

    /**
     * 申请退款原因
     * cause
     */
    private String cause;

    /**
     * 申请退款金额
     * apply_fee
     */
    private BigDecimal applyFee;

    /**
     * 状态 {@link com.chl.victory.beans.enums.order.RefundStatusEnum}
     * status
     */
    private Byte status;

}