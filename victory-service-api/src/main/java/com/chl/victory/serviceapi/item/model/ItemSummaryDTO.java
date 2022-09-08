package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 商品  汇总
 * @author ChenHailong
 * @date 2019/5/28 13:23
 **/
@Data
public class ItemSummaryDTO implements Serializable {
    /**
     * 商品总量
     */
    private Integer total;

    /**
     * 在售商品量
     */
    private Integer onSaleTotal;

    /**
     * 本月下单商品总量
     */
    private Integer currentMonthSoldTotal;
}
