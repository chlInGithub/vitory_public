package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:27
 **/
@Data
public class RefundDTO implements Serializable {
    Date createdTime;
    @NotNull(message = "缺少店铺ID")
    Long shopId;
    @NotNull(message = "缺少买家ID")
    Long buyerId;
    @NotNull(message = "缺少订单ID")
    Long orderId;
    Long id;
    @NotNull(message = "缺少退款类型")
    Byte type;
    @NotNull(message = "缺少退款原因")
    String cause;
    Byte status;
    @NotNull(message = "缺少退款申请金额")
    BigDecimal applyFee;

    /**
     * 节点s
     */
    String process;
}
