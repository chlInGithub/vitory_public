package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/25 10:24
 **/
@Data
public class ShopDeliveryAreaDTO implements Serializable {
    String desc;
    String code;
    String other;
}
