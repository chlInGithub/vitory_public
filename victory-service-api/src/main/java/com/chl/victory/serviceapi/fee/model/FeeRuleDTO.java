package com.chl.victory.serviceapi.fee.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class FeeRuleDTO extends BaseDTO implements Serializable {
    /**
     * 是否统一使用该规则， 1：是
     * uniform
     */
    private Byte uniform;

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 类型，1：固定佣金，2：订单金额比例
     * type
     */
    private Byte type;

    /**
     * 固定佣金或比例
     * val
     */
    private BigDecimal val;

    /**
     * 生效，1：是，0：否
     * valid
     */
    private Byte valid;

}