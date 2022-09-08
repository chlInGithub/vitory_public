package com.chl.victory.serviceapi.sms.model;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SmsHistoryDTO extends BaseDTO implements Serializable {
    /**
     * sms_sending_id
     */
    private Long smsSendingId;

    /**
     * to_mobile
     */
    private Long toMobile;

    /**
     * sms_temp_id
     */
    private Long smsTempId;

    /**
     * values
     */
    private String values;

    /**
     * last_time
     */
    private Date lastTime;

    /**
     * status
     */
    private Byte status;

    /**
     * result
     */
    private String result;

    /**
     * his
     */
    private String his;

    /**
     * send_times
     */
    private Byte sendTimes;

}