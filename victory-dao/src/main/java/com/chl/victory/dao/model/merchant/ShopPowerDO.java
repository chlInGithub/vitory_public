package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;
import java.util.Date;
import javax.persistence.*;

@Table(name = "`shop_power`")
public class ShopPowerDO extends BaseDO {
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

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }
}