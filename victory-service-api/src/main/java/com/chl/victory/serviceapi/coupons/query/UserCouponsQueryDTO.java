package com.chl.victory.serviceapi.coupons.query;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

@Data
public class UserCouponsQueryDTO extends BaseQuery implements Serializable {

    /**
     * 操作者用户ID
     * user_id
     */
    private Long userId;

    /**
     * 优惠券ID
     * coupons_id
     */
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
}