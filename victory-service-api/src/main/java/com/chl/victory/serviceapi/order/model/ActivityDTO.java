package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:28
 **/
@Data
public class ActivityDTO implements Serializable {
    /**
     * 活动ID
     */
    public Long id;
    public String desc;
    /**
     * 参与活动，需满足的金额
     */
    public String meet;
    /**
     * 满足金额条件后，可优惠金额
     */
    public String discount;
    /**
     * 参与该活动，优惠的总金额
     */
    public String totalDis;
    /**
     * 参与该活动前的订单总金额
     */
    public String before;
    /**
     * 参与该活动后订单总金额
     */
    public String after;
}
