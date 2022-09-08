package com.chl.victory.dao.model.order;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/29 18:22
 **/
@Data
@AllArgsConstructor
public class RefundDealFlowNodeDO {
    Integer status;
    Long operatorId;
    String note;
    Date time;
}
