package com.chl.victory.serviceapi.order.query;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

@Data
public class SubOrderQueryDTO extends BaseQuery implements Serializable {
    /**
     * 主订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;

    /**
     * skuID
     * sku_id
     */
    private Long skuId;

    /**
     * 小计金额，按照商品一口价计算
     * total_fee
     */
    private BigDecimal totalFee;

}