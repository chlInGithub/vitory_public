package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/31 14:23
 **/
@Data
public class WxPrePayDTO implements Serializable {
    String timeStamp;

    String nonceStr;

    String prepayId;

    String signType;

    String paySign;

    String orderId;

    String total;

    String shopId;

    String tId;

    String shopName;
}
