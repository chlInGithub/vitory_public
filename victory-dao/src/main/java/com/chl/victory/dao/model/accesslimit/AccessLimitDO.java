package com.chl.victory.dao.model.accesslimit;

import com.chl.victory.dao.model.BaseDO;
import java.util.Date;
import javax.persistence.*;

@Table(name = "`access_limit`")
public class AccessLimitDO extends BaseDO {
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }
}