package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:29
 **/
@Data
public class CouponsDTO implements Serializable {
    /**
     * 优惠ID
     */
    public Long id;
    public String desc;
    /**
     * 优惠券优惠金额
     */
    public String discount;
    public String meet;
    /**
     * 使用该优惠券前的订单金额
     */
    public String before;
    /**
     * 使用该优惠券后的订单金额
     */
    public String after;
}
