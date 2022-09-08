package com.chl.victory.dao.query.item;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuQuery extends BaseQuery {
    /**
     * 归属商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 标题
     * title
     */
    private String title;

    /**
     * 子标题
     * sub_title
     */
    private String subTitle;

    /**
     * 成本
     * cost
     */
    private BigDecimal cost;

    /**
     * 一口价
     * price
     */
    private BigDecimal price;
}