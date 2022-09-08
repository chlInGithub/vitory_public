package com.chl.victory.serviceapi.accesslimit.model;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class AccessLimitDTO extends BaseDTO implements Serializable {
    /**
     * 类型
     * type
     */
    private Integer type;

    /**
     * 统计周期，具体秒数。
     * period
     */
    private Integer period;

    /**
     * 统计周期开始时间
     */
    private Integer startTime;

    /**
     * 统计周期结束时间
     */
    private Integer endTime;

    /**
     * 限流阈值
     * max_limit
     */
    private Integer maxLimit;

    /**
     * 失效时间
     * invalid_time
     */
    private Date invalidTime;
}