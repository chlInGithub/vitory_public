package com.chl.victory.serviceapi.sms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SmsSetHistoryDTO extends BaseDTO implements Serializable {
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

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }
}