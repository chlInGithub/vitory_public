package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SaleStrategyDTO extends BaseDTO implements Serializable {

    /**
     * 营销策略类型
     * strategy_type
     */
    private Byte strategyType;

    /**
     * 属性,json格式
     * attr
     */
    private String attr;

    /**
     * 支持的支付方式 2:线下支付 0:线上支付
     */
    private Integer payType;

    @Data
    public static class PreSellAttr {

        @Positive(message = "订单商品总量必须大于0") Integer minCount;

        @NotEmpty(message = "缺少截止时间") String sentTime;

        @NotEmpty(message = "缺少开始发货时间") String endTime;
    }

    @Data
    public static class MinFeeAttr {
        @Positive(message = "订单实际金额必须大于0") BigDecimal minFee;
    }

    @Data
    public static class MinCountAttr {
        @Positive(message = "订单商品数量必须大于0") Integer minCount;
    }

    @Data
    public static class MaxCountAttr {
        @Positive(message = "订单商品数量必须大于0") Integer maxCount;
    }
}