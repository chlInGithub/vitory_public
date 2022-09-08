package com.chl.victory.serviceapi.order.query;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class RefundOrderQueryDTO extends BaseQuery  implements Serializable {
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