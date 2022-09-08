package com.chl.victory.dao.query.sms;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.util.Date;

/**
 * @author hailongchen9
 */
@Data
public class SmsSetQuery extends BaseQuery {

    /**
     * 生效时间
     * valid_time
     */
    private Date startValidTime;
    private Date endValidTime;

    /**
     * 状态，0失效，1生效
     * status
     */
    private Boolean status;

    private Long itemId;
}