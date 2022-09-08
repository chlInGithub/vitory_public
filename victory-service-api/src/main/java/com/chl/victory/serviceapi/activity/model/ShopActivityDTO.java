package com.chl.victory.serviceapi.activity.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class ShopActivityDTO extends BaseDTO implements Serializable {
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
     * 状态，0：失效，忽略生效范围，记录失效；1：生效，生效且生效范围内才可用
     * status
     * @see com.chl.victory.serviceapi.activity.enums.ActivityConponsStatusEnum
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
     * 参与活动，需满足的金额
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
     * @see com.chl.victory.serviceapi.activity.enums.OnlyUseEnum
     */
    private Boolean only;

    /**
     * 同一订单重复使用，如每100减10，则200减20？0：不可以；1：可以
     * repeat
     * @see com.chl.victory.serviceapi.activity.enums.RepeatUseEnum
     */
    private Boolean repeat;

    /**
     * 优惠、活动的执行顺序，越大则提前计算
     * order
     */
    private Integer order;

    private String img;
}