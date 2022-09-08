package com.chl.victory.dao.query.activity;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.util.Date;

@Data
public class ShopActivityQuery extends BaseQuery {
    /**
     * 生效时间
     * valid_time
     */
    private Date startValidTime;
    private Date endValidTime;

    /**
     * 状态，0：失效，忽略生效范围，记录失效；1：生效，生效且生效范围内才可用
     * status
     */
    private Boolean status;

    /**
     * 标题
     * title
     */
    private String title;

    /**
     * 可与其他优惠、活动同时使用？0：不可以；1：可以
     * only
     */
    private Boolean only;

    /**
     * 同一订单重复使用，如每100减10，则200减20？0：不可以；1：可以
     * repeat
     */
    private Boolean repeat;

    /**
     * 优惠、活动的执行顺序，越大则提前计算
     * order
     */
    private Byte order;
}