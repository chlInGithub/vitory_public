package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:06
 **/
@Data
public class SubOrderDTO extends BaseDTO implements Serializable {

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;
    private String itemTitle;
    private String itemImg;

    /**
     * skuID
     * sku_id
     */
    private Long skuId;
    private String skuTitle;

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

    private String presell;
}
