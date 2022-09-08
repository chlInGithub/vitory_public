package com.chl.victory.serviceapi.sms.model;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SmsSetDTO extends BaseDTO  implements Serializable {
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
     * @see com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum
     */
    private Boolean status;

}