package com.chl.victory.dao.model.order;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDeliverDO extends BaseDO {
    /**
     * 订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 收货人手机号，来自member_deliver，使用冗余数据，考虑简化查询以及修改地址不影响历史订单
     * mobile
     */
    private Long mobile;

    /**
     * 收货人
     * name
     */
    private String name;

    /**
     * 收货地址
     * addr
     */
    private String addr;

    /**
     * 配送方式,0:不需要物流;1:需要物流
     * type
     */
    private Byte type;

    /**
     * 物流公司名称
     * logistics_cp
     */
    private String logisticsCp;

    /**
     * 物流单编号
     * logistics_no
     */
    private String logisticsNo;

    /**
     * 运费
     * freight
     */
    private BigDecimal freight;
}