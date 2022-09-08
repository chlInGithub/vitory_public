package com.chl.victory.serviceapi.order.query;
import java.io.Serializable;

import com.chl.victory.serviceapi.BaseQuery;

/**
 * @author hailongchen9
 */
public class OrderDeliverQueryDTO extends BaseQuery implements Serializable {
    /**
     * 订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 配送方式,0:不需要物流;1:需要物流
     * type
     * @see com.chl.victory.serviceapi.order.enums.DeliverTypeEnum
     */
    private Byte type;

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}