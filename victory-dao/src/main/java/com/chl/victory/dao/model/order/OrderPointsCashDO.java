package com.chl.victory.dao.model.order;

import lombok.Data;

/**
 * 订单中使用的基金兑换情况
 * @author ChenHailong
 * @date 2019/5/23 16:18
 **/
@Data
public class OrderPointsCashDO {
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
