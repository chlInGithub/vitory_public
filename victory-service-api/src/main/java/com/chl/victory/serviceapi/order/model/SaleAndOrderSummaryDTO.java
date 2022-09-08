package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 销售额  订单  汇总
 * @author ChenHailong
 * @date 2019/5/28 13:23
 **/
@Data
public class SaleAndOrderSummaryDTO implements Serializable {
    private SaleSummaryDTO saleSummary;
    private OrderSummaryDTO orderSummary;

    @Data
    public static class SaleSummaryDTO  implements Serializable{

        /**
         * 总销售额
         */
        private BigDecimal total;

        /**
         * 本月总销售额
         */
        private BigDecimal currentMonthTotal;
    }

    @Data
    public static class OrderSummaryDTO implements Serializable {

        /**
         * 总订单量
         */
        private Integer total;

        /**
         * 本月订单量
         */
        private Integer currentMonthTotal;

        /**
         * 客单价
         */
        private BigDecimal avgOrderFee;
        /**
         * 本月客单价
         */
        private BigDecimal currentMonthAvgOrderFee;

        /**
         * 本月已付款单量
         */
        private Integer currentMonthPayedOrders;

        /**
         * 本月未付款单量
         */
        private Integer currentMonthNotPayedOrders;
    }
}
