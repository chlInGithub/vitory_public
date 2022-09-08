package com.chl.victory.serviceapi.order.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

/**
 * 订单查询参数，可定制查询详情，如支付单、子订单、买家信息、收货配送信息、状态流、优惠信息
 * @author ChenHailong
 * @date 2019/5/28 13:58
 **/
@Data
public class OrderQueryDTO implements Serializable {
    boolean needPayOrder;
    boolean needSubOrder;
    boolean needBuyerInfo;
    boolean needDeliverInfo;
    boolean needStatusFlow;
    boolean needRefund;
    boolean needCoupons;

    /**
     * 会员名称
     */
    String buyerName;

    /**
     * 会员手机号
     */
    Long buyerMobile;

    /**
     * 商品ID
     */
    Long itemId;

    /**
     * 是否为店铺增值业务，0不是，1是
     * shop_service
     */
    private Boolean shopService;

    /**
     * 状态，10:new待付款;20:payed待发货;30:sent待收货;40:success交易完成;50:close交易关闭;60:askRefund申请退款;61:refunding退款处理中;62:refunded退款完成;63:closeRefund退款关闭
     * status
     */
    private Byte status;

    /**
     * 店铺会员ID，购买商品前用户需成为店铺会员；购买增值业务时，对应店主用户ID
     * buyer_id
     */
    private Long buyerId;

    /**
     * 仅商家购买店铺增值业务时可为null；收货信息ID，为用户维护收货信息
     * order_deliver_id
     */
    private Long orderDeliverId;

    /**
     * 退款单ID
     * refund_id
     */
    private Long refundId;

    /**
     * 订单小计金额，按照商品一口价计算
     * total_fee
     */
    private BigDecimal totalFee;

    /**
     * 活动详情，json格式
     * activity
     */
    private String activity;

    /**
     * 优惠券详情,json格式
     * coupons
     */
    private String coupons;

    /**
     * 积分抵现金额，json形式
     * points_cash
     */
    private String pointsCash;

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

    private Byte payType;

    private Byte deliverType;

    String appId;

    List<Long> ids;
    Date startedCreatedTime;
    Date endedCreatedTime;
    Date startedModifiedTime;
    Date endedModifiedTime;
    /**
     * 页码，base 0
     */
    Integer pageIndex;
    /**
     * 单页记录数
     */
    Integer pageSize;
    Integer rowStart;

    /**
     * 单列排序优先
     */
    String orderColumn;
    /**
     * 默认倒序
     */
    boolean desc = true;

    /**
     * 仅查询部分信息
     */
    boolean justOutline;

    Integer presell;

    /**
     * 支持多列排序，但单列排序指定列时优先
     */
    List<OrderedColumn> orderedColumns;

    /**
     * record ID,按照一定规则生成
     */
    Long id;
    /**
     * record created time
     */
    Date createdTime;
    /**
     * record modified time
     */
    Date modifiedTime;

    /**
     * belong to shop ID
     */
    @NotNull
    @Positive
    Long shopId;

    /**
     * operating user id
     */
    Long operatorId;

    public Integer getRowStart() {
        if (null != pageIndex && null != pageSize){
            return pageIndex * pageSize;
        }
        return null;
    }

    @Data
    public static class OrderedColumn{
        String orderColumn;
        boolean desc = true;
    }

    public void needAllInfo(){
        needPayOrder = true;
        needSubOrder = true;
        needBuyerInfo = true;
        needDeliverInfo = true;
        needStatusFlow = true;
        needRefund = true;
        needCoupons = true;
    }
}
