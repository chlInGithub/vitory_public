package com.chl.victory.serviceapi.item.query;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

@Data
public class SkuQueryDTO extends BaseQuery implements Serializable {
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