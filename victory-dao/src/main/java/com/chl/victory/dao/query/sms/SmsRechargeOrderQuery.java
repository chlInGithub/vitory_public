package com.chl.victory.dao.query.sms;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class SmsRechargeOrderQuery extends BaseQuery {
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