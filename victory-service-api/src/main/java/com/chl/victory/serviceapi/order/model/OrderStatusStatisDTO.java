package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import lombok.Data;

/**
 * 状态维度统计
 * @author ChenHailong
 * @date 2019/6/4 11:21
 **/
@Data
public class OrderStatusStatisDTO implements Serializable {
    OrderStatusEnum orderStatusEnum;
    Integer count;
}
