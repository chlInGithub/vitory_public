package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:31
 **/
public class PointsCashDTO implements Serializable {
    /**
     * 本次使用积分数量
     */
    public Integer used;
    /**
     * 积分数量余额
     */
    public Integer balance;
    /**
     * 本次积分折现金额
     */
    public String cash;
}
