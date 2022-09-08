package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.chl.victory.serviceapi.BaseDTO;
import com.chl.victory.serviceapi.order.enums.PayOrderFeeRightEnum;
import com.chl.victory.serviceapi.order.enums.PayOrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:22
 **/
@Data
public class PayOrderDTO extends BaseDTO implements Serializable {

    /**
     * 支付类型 0 wechart; 1 alipay
     * type
     */
    private PayTypeEnum type;

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
     * 系统自身生成的支付编号
     * pay_no
     */
    private String payNo;

    /**
     * 状态，0:新建/待支付；1:已支付
     * status
     */
    private PayOrderStatusEnum status;

    /**
     * 检查支付金额对吗？0：不对；1：对
     * check
     * @see PayOrderFeeRightEnum
     */
    private PayOrderFeeRightEnum check;

    /**
     * 超时时间，该时间后支付单被删除
     * timeout
     */
    private Date timeout;
}
