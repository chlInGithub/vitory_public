package com.chl.victory.dao.model.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单中使用的优惠情况
 * @author ChenHailong
 * @date 2019/5/23 16:19
 **/
@Data
public class OrderCouponsDO {
    /**
     * 优惠ID
     */
    public Long id;
    public String desc;
    /**
     * 优惠券优惠金额
     */
    public BigDecimal discount;
    public BigDecimal meet;
    /**
     * 使用该优惠券前的订单金额
     */
    public BigDecimal before;
    /**
     * 使用该优惠券后的订单金额
     */
    public BigDecimal after;
}
