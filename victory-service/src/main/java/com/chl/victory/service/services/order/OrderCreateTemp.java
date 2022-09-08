package com.chl.victory.service.services.order;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.exception.BusServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/8/31 18:09
 *
 */
 @Data
public class OrderCreateTemp {

    @NotNull(message = "缺少店铺ID") Long shopId;

    @NotNull(message = "缺少买家ID") Long buyerId;

    @NotNull(message = "缺少appId") String appId;

    /**
     * 商品 元素格式为itemId_skuId_count
     */
    @NotEmpty(message = "缺少商品信息") @NotNull(message = "缺少商品信息") List<String> items;

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
    @NotBlank(message = "缺少收货地址ID") String deliverId;

    /**
     * 运费模板ID
     */
    String freightTempId;

    /**
     * 交付类型，自提或快递
     * @see com.chl.victory.serviceapi.order.enums.DeliverTypeEnum
     */
    @NotBlank(message = "缺少交付类型") Integer deliverType;

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
    @NotBlank(message = "缺少参考结算总金额")
    @Transient
    String referTotal;

    List<ItemOfNewOrder> itemOfNewOrders;

    Integer payType;

    private String buyerImg;

    public List<ItemOfNewOrder> parseItemsOfNewOrder() throws Exception {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }

        if (null != itemOfNewOrders) {
            return itemOfNewOrders;
        }

        List<ItemOfNewOrder> itemsOfNewOrder = new ArrayList<>();

        for (String item : items) {
            if (StringUtils.isBlank(item)) {
                throw new BusServiceException("商品参数错误:存在empty数据");
            }
            String[] temp = item.split("_");
            Long itemId = NumberUtils.toLong(temp[ 0 ], -1);
            Long skuId = NumberUtils.toLong(temp[ 1 ], -1);
            Integer count = NumberUtils.toInt(temp[ 2 ], -1);
            if (-1 == itemId || -1 == skuId || -1 == count) {
                throw new BusServiceException("缺少商品参数");
            }
            ItemOfNewOrder itemOfNewOrder = new ItemOfNewOrder(itemId, skuId, count);
            itemsOfNewOrder.add(itemOfNewOrder);
        }

        this.itemOfNewOrders = itemsOfNewOrder;

        return this.itemOfNewOrders;
    }

    @Data
    @AllArgsConstructor
    public class ItemOfNewOrder {

        Long itemId;

        Long skuId;

        Integer count;
    }
}
