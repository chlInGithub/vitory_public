package com.chl.victory.dao.model.sms;

import com.chl.victory.dao.model.BaseDO;
import java.util.Date;

public class SmsSetDO extends BaseDO {
    /**
     * 对应商品ID，创建套餐时系统自动创建关联商品
     * item_id
     */
    private Long itemId;

    /**
     * 短信条数
     * num
     */
    private Integer num;

    /**
     * 生效时间
     * valid_time
     */
    private Date validTime;

    /**
     * 失效时间
     * invalid_time
     */
    private Date invalidTime;

    /**
     * 状态，0失效，1生效
     * status
     */
    private Boolean status;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Date getValidTime() {
        return validTime;
    }

    public void setValidTime(Date validTime) {
        this.validTime = validTime;
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}