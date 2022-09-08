package com.chl.victory.dao.model.sms;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

@Data
public class SmsRechargeOrderDO extends BaseDO {
    /**
     * 对应订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 状态，与order保持同步
     * status
     */
    private Byte status;

}