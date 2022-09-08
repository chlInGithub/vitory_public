package com.chl.victory.dao.model.coupons;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ShopCouponsDO extends BaseDO {
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
     * 描述
     * desc
     */
    private String desc;

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
     * 是否排斥其他优惠、活动？0：不排斥，可共同使用；1：排斥，仅单独使用
     * only
     */
    private Boolean only;

    /**
     * 计算优惠的优先级，越大则提前计算
     * priority
     */
    private Byte priority;

}