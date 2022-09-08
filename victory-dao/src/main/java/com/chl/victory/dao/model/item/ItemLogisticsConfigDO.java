package com.chl.victory.dao.model.item;

import lombok.Data;

/**商品的物流配置
 * @author ChenHailong
 * @date 2019/5/23 10:25
 **/
@Data
public class ItemLogisticsConfigDO{
    /**
     * 发货地
     */
    String from;
    /**
     * 需要物流，0不需要， 1 需要
     */
    Integer needLogistics;
    /**
     * 运费策略 0无运费， 1固定运费，……
     */
    Integer freightStrategy;
    /**
     * for 1固定运费
     */
    Integer sameFreightVal;
}
