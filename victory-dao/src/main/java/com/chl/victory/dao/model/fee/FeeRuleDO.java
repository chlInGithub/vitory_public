package com.chl.victory.dao.model.fee;

import com.chl.victory.dao.model.BaseDO;
import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "`fee_rule`")
public class FeeRuleDO extends BaseDO {
    /**
     * 是否统一使用该规则， 1：是
     * uniform
     */
    private Byte uniform;

    /**
     * 商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 类型，1：固定佣金，2：订单金额比例
     * type
     */
    private Byte type;

    /**
     * 固定佣金或比例
     * val
     */
    private BigDecimal val;

    /**
     * 生效，1：是，0：否
     * valid
     */
    private Byte valid;

    public Byte getUniform() {
        return uniform;
    }

    public void setUniform(Byte uniform) {
        this.uniform = uniform;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public BigDecimal getVal() {
        return val;
    }

    public void setVal(BigDecimal val) {
        this.val = val;
    }

    public Byte getValid() {
        return valid;
    }

    public void setValid(Byte valid) {
        this.valid = valid;
    }
}