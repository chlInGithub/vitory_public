package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用于创建订单
 * @author ChenHailong
 * @date 2019/10/10 15:17
 **/
@Data
public class OrderCreateDTO implements Serializable {

    Long shopId;

    Long buyerId;

    String appId;

    /**
     * 商品 元素格式为itemId_skuId_count
     */
    List<String> items;

    /**
     * 店铺活动ID
     */
    String actId;

    /**
     * 优惠券ID
     */
    String couId;

    /**
     * 收货信息ID
     */
    String deliverId;

    /**
     * 运费模板ID
     */
    String freightTempId;

    /**
     * 交付类型，自提或快递
     * @see com.chl.victory.serviceapi.order.enums.DeliverTypeEnum
     */
    Integer deliverType;

    /**
     * 快递费用
     */
    String deliverFee;

    /**
     * 备注
     */
    String note;

    /**
     * 参考结算总金额，用于结算页结果与下单重计算金额对比
     */
    String referTotal;

    Integer payType;

    private String buyerImg;
}

