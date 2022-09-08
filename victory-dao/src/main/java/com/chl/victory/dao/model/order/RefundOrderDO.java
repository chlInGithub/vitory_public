package com.chl.victory.dao.model.order;

import com.alibaba.fastjson.JSON;
import com.chl.victory.dao.model.BaseDO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RefundOrderDO extends BaseDO {
    /**
     * 主订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 退款类型，1退货退款  2仅退款   3换货
     * type
     */
    private Byte type;

    /**
     * 申请退款原因
     * cause
     */
    private String cause;

    /**
     * 申请退款金额
     * apply_fee
     */
    private BigDecimal applyFee;

    /**
     * 状态，1申请/商家处理中；2商家同意/退款中；3已退款；4拒绝
     * status
     */
    private Byte status;
    private Byte preStatus;

    /**
     * 过程记录，json形式
     * process
     */
    private String process;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause == null ? null : cause.trim();
    }

    public BigDecimal getApplyFee() {
        return applyFee;
    }

    public void setApplyFee(BigDecimal applyFee) {
        this.applyFee = applyFee;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process == null ? null : process.trim();
    }

    public List<RefundDealFlowNodeDO> getProcessDealFlowNodeDOS(){
        if (process == null || process.trim().length() <= 0){
            // 便于外部直接add ele
            return new ArrayList<>();
        }
        return JSON.parseArray(process, RefundDealFlowNodeDO.class);
    }
}