package com.chl.victory.dao.model.item;



import lombok.Data;

import java.math.BigDecimal;

/**商品属性
 * @author ChenHailong
 * @date 2019/5/23 10:26
 **/
@Data
public class ItemAttributeDO{
    /**
     * 重量，单位KG
     */
    BigDecimal weight;
    /**
     * 体积，单位立方米
     */
    BigDecimal volume;
}
