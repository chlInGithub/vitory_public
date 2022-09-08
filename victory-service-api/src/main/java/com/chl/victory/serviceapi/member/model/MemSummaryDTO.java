package com.chl.victory.serviceapi.member.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 会员  汇总
 * @author ChenHailong
 * @date 2019/5/28 13:23
 **/
@Data
public class MemSummaryDTO implements Serializable {
    /**
     * 用户总量
     */
    private Integer total;

    /**
     * 本月下单用户量
     */
    private Integer currentMonthShoppingTotal;

    /**
     * 本月新增用户量
     */
    private Integer currentMonthNewTotal;
}
