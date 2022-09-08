package com.chl.victory.dao.query.sms;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/7 11:51
 **/
@Data
public class SmsHistoryQuery extends SmsSendingQuery {
    /**
     * sms_sending_id
     */
    private Long smsSendingId;
}
