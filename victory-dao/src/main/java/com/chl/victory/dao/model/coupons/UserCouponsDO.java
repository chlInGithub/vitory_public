package com.chl.victory.dao.model.coupons;

import com.chl.victory.dao.model.BaseDO;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Table(name = "`user_coupons`")
public class UserCouponsDO extends BaseDO {
    /**
     * 操作者用户ID
     * user_id
     */
    @NotNull
    @Positive
    private Long userId;

    /**
     * 优惠券ID
     * coupons_id
     */
    @NotNull
    @Positive
    private Long couponsId;

    /**
     * 使用时间
     * used_time
     */
    private Date usedTime;

    /**
     * 状态，0：未使用；1：已使用
     * status
     */
    private Boolean status;

    /**
     * expiry_time
     */
    private Date expiryTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCouponsId() {
        return couponsId;
    }

    public void setCouponsId(Long couponsId) {
        this.couponsId = couponsId;
    }

    public Date getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(Date usedTime) {
        this.usedTime = usedTime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }
}