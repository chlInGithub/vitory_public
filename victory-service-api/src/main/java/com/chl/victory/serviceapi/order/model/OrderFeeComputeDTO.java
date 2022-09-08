package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 用于计算订单金额
 *
 * @author ChenHailong
 * @date 2019/10/10 15:17
 **/
@Data
public class OrderFeeComputeDTO implements Serializable {
    @NotNull(message = "缺少店铺ID")
    Long shopId;
    @NotNull(message = "缺少买家ID")
    Long buyerId;
    /**
     * 商品 元素格式为itemId_skuId_count
     */
    @NotEmpty(message = "缺少商品信息")
    @NotNull(message = "缺少商品信息")
    List<String> items;
    /**
     * 店铺活动ID
     */
    String actId;
    /**
     * 优惠券ID
     */
    String couId;
}

