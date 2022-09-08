package com.chl.victory.dao.model.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单参与的活动
 * @author ChenHailong
 * @date 2019/5/23 16:16
 **/
@Data
public class OrderActivityDO {
    /**
     * 活动ID
     */
    public Long id;
    public String desc;
    /**
     * 参与活动，需满足的金额
     */
    public BigDecimal meet;
    /**
     * 满足金额条件后，可优惠金额
     */
    public BigDecimal discount;
    /**
     * 参与该活动，优惠的总金额
     */
    public BigDecimal totalDis;
    /**
     * 参与该活动前的订单总金额
     */
    public BigDecimal before;
    /**
     * 参与该活动后订单总金额
     */
    public BigDecimal after;
}
