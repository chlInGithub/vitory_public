package com.chl.victory.dao.query.order;

import com.chl.victory.dao.query.BaseQuery;

/**
 * @author hailongchen9
 */
public class OrderDeliverQuery extends BaseQuery {
    /**
     * 订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 配送方式,0:不需要物流;1:需要物流
     * type
     * @see DeliverTypeEnum
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