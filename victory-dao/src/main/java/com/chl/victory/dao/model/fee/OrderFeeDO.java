package com.chl.victory.dao.model.fee;

import com.chl.victory.dao.model.BaseDO;
import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "`order_fee`")
public class OrderFeeDO extends BaseDO {
    /**
     * 用户ID
     * user_id
     */
    private Long userId;

    /**
     * 佣金归属年月 yyyyMM
     * statistic_date
     */
    private Integer statisticDate;

    /**
     * 订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 子订单ID
     * sub_id
     */
    private Long subId;

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 佣金
     * fee
     */
    private BigDecimal fee;

    /**
     * 佣金规则快照
     * rule
     */
    private String rule;

    /**
     * 1：待审核，2：待支付，3：已支付
     * status
     */
    private Byte status;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatisticDate() {
        return statisticDate;
    }

    public void setStatisticDate(Integer statisticDate) {
        this.statisticDate = statisticDate;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSubId() {
        return subId;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule == null ? null : rule.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}