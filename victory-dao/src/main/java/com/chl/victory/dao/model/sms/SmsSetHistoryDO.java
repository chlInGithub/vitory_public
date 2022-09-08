package com.chl.victory.dao.model.sms;

import com.chl.victory.dao.model.BaseDO;
import java.math.BigDecimal;
import java.util.Date;

public class SmsSetHistoryDO extends BaseDO {
    /**
     * sms_set_id
     */
    private Long smsSetId;

    /**
     * item_id
     */
    private Long itemId;

    /**
     * 短信套餐描述,历史冗余关键数据
     * desc
     */
    private String desc;

    /**
     * 套餐标价
     * tag_price
     */
    private BigDecimal tagPrice;

    /**
     * 一口价
     * price
     */
    private BigDecimal price;

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

    public Long getSmsSetId() {
        return smsSetId;
    }

    public void setSmsSetId(Long smsSetId) {
        this.smsSetId = smsSetId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public BigDecimal getTagPrice() {
        return tagPrice;
    }

    public void setTagPrice(BigDecimal tagPrice) {
        this.tagPrice = tagPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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