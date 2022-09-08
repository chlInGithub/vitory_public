package com.chl.victory.dao.query.sms;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.util.Date;

/**
 * @author hailongchen9
 */
@Data
public class SmsSendingQuery extends BaseQuery {
    /**
     * 短信接收者手机号
     * to_mobile
     */
    private Long toMobile;

    /**
     * 短信模板ID
     * sms_temp_id
     */
    private Long smsTempId;

    /**
     * 应用于短信模板的具体数据值，间隔符为|
     * values
     */
    private String values;

    /**
     * 上次发送时间
     * last_time
     */
    private Date lastTime;

    /**
     * 发送结果状态，1:新建；2成功；3失败
     * status
     */
    private Byte status;

    /**
     * 发送具体结果
     * result
     */
    private String result;

    /**
     * 已尝试发送次数
     * send_times
     */
    private Boolean sendTimes;

}