package com.chl.victory.serviceapi.sms.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SmsRechargeOrderDTO extends BaseDTO  implements Serializable {
    /**
     * 对应订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 状态，与order保持同步
     * status
     * @see com.chl.victory.serviceapi.order.enums.OrderStatusEnum
     */
    private Byte status;

}