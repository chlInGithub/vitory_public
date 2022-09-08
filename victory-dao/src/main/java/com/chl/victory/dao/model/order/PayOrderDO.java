package com.chl.victory.dao.model.order;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayOrderDO extends BaseDO {
    /**
     * 主订单ID
     * order_id
     */
    private Long orderId;

    /**
     * 支付类型 0 wechart; 1 alipay
     * type
     */
    private Byte type;

    /**
     * 描述
     * desc
     */
    private String desc;

    /**
     * 需支付金额
     * pay_fee
     */
    private BigDecimal payFee;

    /**
     * 系统自身生成的支付编号，暂不使用，用ID
     * pay_no
     */
    private String payNo;

    /**
     * 第三方支付相关，json格式，如入参与结果
     * context
     */
    private String context;

    /**
     * 状态，0:新建/待支付；1:已支付
     * status
     */
    private Boolean status;

    /**
     * 检查支付金额对吗？0：不对；1：对
     * check
     */
    private Boolean check;

    /**
     * 超时时间，该时间后支付单被删除
     * timeout
     */
    private Date timeout;


}