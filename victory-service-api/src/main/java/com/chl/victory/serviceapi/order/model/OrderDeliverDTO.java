package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.chl.victory.serviceapi.BaseDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:23
 **/
@Data
public class OrderDeliverDTO extends BaseDTO implements Serializable {

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
    private String address;
    private String city;

    /**
     * 配送方式,0:不需要物流;1:需要物流
     * type
     */
    private DeliverTypeEnum type;

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
