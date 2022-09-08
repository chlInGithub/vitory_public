package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**商品的物流配置
 * @author ChenHailong
 * @date 2019/5/23 10:25
 **/
@Data
public class ItemLogisticsConfigDTO implements Serializable {
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

    public static ItemLogisticsConfigDTO getLogisticsConfig(String itemLogistics) {
        if (null == itemLogistics || itemLogistics.trim().equals("")) {
            return null;
        }
        ItemLogisticsConfigDTO itemLogisticsConfigDO = JSONObject
                .parseObject(itemLogistics, ItemLogisticsConfigDTO.class);
        return itemLogisticsConfigDO;
    }
}
