package com.chl.victory.dao.query.coupons;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ShopCouponsQuery extends BaseQuery {
    /**
     * 生效时间
     * valid_time
     */
    private Date startValidTime;
    private Date endValidTime;

    /**
     * 状态，0：失效；1：生效
     * status
     */
    private Boolean status;

    /**
     * 标题
     * title
     */
    private String title;

    /**
     * 需满足的最低金额
     * meet
     */
    private BigDecimal meet;

    /**
     * 满足金额条件后，可优惠金额
     * discount
     */
    private BigDecimal discount;

    /**
     * 可与其他优惠、活动同时使用？0：不可以；1：可以
     * only
     */
    private Boolean only;

    /**
     * 计算优惠的优先级，越大则提前计算
     * priority
     */
    private Byte priority;
}