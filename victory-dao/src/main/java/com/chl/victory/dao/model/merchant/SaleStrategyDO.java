package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import javax.persistence.*;

@Table(name = "`sale_strategy`")
@Data
public class SaleStrategyDO extends BaseDO {
    /**
     * 营销策略类型
     * strategy_type
     */
    private Byte strategyType;

    /**
     * 属性,json格式
     * attr
     */
    private String attr;

    /**
     * 支持的支付方式 2:线下支付 0:线上支付
     */
    private Integer payType;

    public Byte getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(Byte strategyType) {
        this.strategyType = strategyType;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr == null ? null : attr.trim();
    }

}