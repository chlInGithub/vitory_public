package com.chl.victory.dao.model.order;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubOrderDO extends BaseDO {
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
     * 一口价
     * price
     */
    private BigDecimal price;

    /**
     * 数量
     * count
     */
    private Byte count;

    /**
     * 小计金额，按照商品一口价计算
     * total_fee
     */
    private BigDecimal totalFee;

    /**
     * 活动详情，json格式
     * activity
     */
    private String activity;

    /**
     * 优惠券详情,json格式
     * coupons
     */
    private String coupons;

    /**
     * 积分抵现金额，json形式
     * points_cash
     */
    private String pointsCash;

    /**
     * 订单实际金额，订单小计金额-活动-优惠-积分抵现等
     * real_fee
     */
    private BigDecimal realFee;

    private Long shareUserId;

    private String presell;
}