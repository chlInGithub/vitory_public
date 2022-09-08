package com.chl.victory.serviceapi.cart.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.order.model.ActivityDTO;
import com.chl.victory.serviceapi.order.model.CouponsDTO;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/10/28 18:07
 **/
@Data
public class CartComputeDTO implements Serializable {
    /**
     * 订单小计金额，按照商品一口价计算
     * total_fee
     */
    private BigDecimal totalFee;
    /**
     * 订单实际金额，订单小计金额-活动-优惠-积分抵现等
     * real_fee
     */
    private BigDecimal realFee;
    /**
     * 商品购买数量
     */
    private Integer itemCount;
    ActivityDTO activityDTO;
    CouponsDTO couponsDTO;
}
