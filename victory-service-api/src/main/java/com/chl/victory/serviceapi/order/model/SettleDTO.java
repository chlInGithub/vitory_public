package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import lombok.Data;

/**
 * 下单数据模型
 *
 * @author ChenHailong
 * @date 2019/10/29 13:56
 **/
@Data
public class SettleDTO implements Serializable {
    Map<Long, ItemDTO> itemIdMap = new HashMap<>();
    Map<Long, SkuDTO> skuIdMap = new HashMap<>();
    OrderDeliverDTO orderDeliverDTO;
    ActivityDTO activityDTO;
    CouponsDTO couponsDTO;
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
}
