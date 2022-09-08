package com.chl.victory.serviceapi.order.model;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 15:08
 **/
@Data
public class StatusFlowNodeDTO implements Serializable {

    public OrderStatusEnum status;

    public Date time;
}
