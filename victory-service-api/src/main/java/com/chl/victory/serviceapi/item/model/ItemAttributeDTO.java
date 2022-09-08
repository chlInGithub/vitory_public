package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**商品属性
 * @author ChenHailong
 * @date 2019/5/23 10:26
 **/
@Data
public class ItemAttributeDTO implements Serializable {
    /**
     * 重量，单位KG
     */
    BigDecimal weight;
    /**
     * 体积，单位立方米
     */
    BigDecimal volume;
}
