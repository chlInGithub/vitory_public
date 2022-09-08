package com.chl.victory.dao.model.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chl.victory.dao.model.BaseDO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDO extends BaseDO {
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
    private Byte preStatus;
    private boolean refunding;

    /**
     * 状态流转，json形式，包括状态与时间
     * status_flow
     */
    private String statusFlow;

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
     * 用于查询
     */
    private Long currentRefundId;

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

    /**
     * 归属的小程序
     */
    String appId;

    public List<OrderCouponsDO> getOrderCouponsDOs(){
        if (coupons == null || coupons.trim().length() <= 0){
            return Collections.EMPTY_LIST;
        }
        if (coupons.startsWith("[")) {
            return JSON.parseArray(coupons, OrderCouponsDO.class);
        }
        return Arrays.asList(JSONObject.parseObject(coupons, OrderCouponsDO.class));
    }
    public List<OrderActivityDO> getOrderActivityDOS(){
        if (activity == null || activity.trim().length() <= 0){
            return Collections.EMPTY_LIST;
        }
        if (activity.startsWith("[")) {
            return JSON.parseArray(activity, OrderActivityDO.class);
        }

        return Arrays.asList(JSONObject.parseObject(activity, OrderActivityDO.class));
    }
    public OrderPointsCashDO getOrderPointsCashDO(){
        if (pointsCash == null || pointsCash.trim().length() <= 0){
            return null;
        }
        return JSON.parseObject(pointsCash, OrderPointsCashDO.class);
    }
    public List<OrderStatusFlowNodeDO> getStatusFlowNodeDOS(){
        if (statusFlow == null || statusFlow.trim().length() <= 0){
            // 便于外部直接add ele
            return new ArrayList<>();
        }
        return JSON.parseArray(statusFlow, OrderStatusFlowNodeDO.class);
    }
}