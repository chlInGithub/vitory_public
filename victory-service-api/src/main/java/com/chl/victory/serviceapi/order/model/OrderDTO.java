package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.chl.victory.serviceapi.BaseDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 13:01
 **/
@Data
public class OrderDTO extends BaseDTO implements Serializable {
    /**
     * 是否为店铺增值业务，0不是，1是
     * shop_service
     */
    private Boolean shopService;

    /**
     * 状态，10:new待付款;20:payed待发货;30:sent待收货;40:success交易完成;50:close交易关闭;60:askRefund申请退款;61:refunding退款处理中;62:refunded退款完成;63:closeRefund退款关闭
     * status
     */
    private OrderStatusEnum status;

    /**
     * 状态流转，包括状态与时间
     * status_flow
     */
    private List<StatusFlowNodeDTO> statusFlow;

    private List<SubOrderDTO> subOrders;

    /**
     * 店铺会员ID，购买商品前用户需成为店铺会员；购买增值业务时，对应店主用户ID
     * buyer_id
     */
    private Long buyerId;
    private ShopMemberDTO buyer;

    /**
     * 仅商家购买店铺增值业务时可为null；收货信息ID，为用户维护收货信息
     * order_deliver_id
     */
    private Long orderDeliverId;
    private OrderDeliverDTO orderDeliver;

    /**
     * 退款单ID
     * refund_id
     */
    private Long refundId;
    private RefundDTO refund;

    /**
     * 买家留言
     * buyer_msg
     */
    private String buyerMsg;

    /**
     * 备注
     * note
     */
    private String note;

    /**
     * 订单小计金额，按照商品一口价计算
     * total_fee
     */
    private BigDecimal totalFee;

    /**
     * 活动详情
     * activity
     */
    private List<ActivityDTO> activity;

    /**
     * 优惠券详情
     * coupons
     */
    private List<CouponsDTO> coupons;

    /**
     * 积分抵现金额，json形式
     * points_cash
     */
    private PointsCashDTO pointsCash;

    /**
     * 订单实际金额，订单小计金额-活动-优惠-积分抵现等
     * real_fee
     */
    private BigDecimal realFee;

    /**
     * 创建订单时，生成支付单
     * pay_id
     */
    private Long payId;
    private PayOrderDTO payOrder;

    String appId;
}
