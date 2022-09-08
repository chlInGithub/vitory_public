package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopPowerDTO extends BaseDTO implements Serializable {
    /**
     * 增值能力类型
     * type
     */
    private Byte type;

    /**
     * 能力范围，包括级别、上限,{level:,xMax:}
     * detail
     */
    private String detail;

    /**
     * 失效时间
     * expired_time
     */
    private Date expiredTime;

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

}