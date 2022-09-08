package com.chl.victory.dao.model.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * @author ChenHailong
 * @date 2019/5/29 18:22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusFlowNodeDO {
    Integer status;
    Long operatorId;
    Date time;
    String context;

    public OrderStatusFlowNodeDO(Integer status, Long operatorId, Date time) {
        this.status = status;
        this.operatorId = operatorId;
        this.time = time;
    }
}
