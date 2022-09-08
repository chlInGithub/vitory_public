package com.chl.victory.dao.query.order;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubOrderQuery extends BaseQuery {
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