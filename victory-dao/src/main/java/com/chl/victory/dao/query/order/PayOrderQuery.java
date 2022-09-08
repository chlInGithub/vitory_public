package com.chl.victory.dao.query.order;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author hailongchen9
 */
@Data
public class PayOrderQuery extends BaseQuery {
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
     * 需支付金额
     * pay_fee
     */
    private BigDecimal payFee;

    /**
     * 系统自身生成的支付编号
     * pay_no
     */
    private String payNo;

    /**
     * 状态，1:新建/待支付；2:已支付
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