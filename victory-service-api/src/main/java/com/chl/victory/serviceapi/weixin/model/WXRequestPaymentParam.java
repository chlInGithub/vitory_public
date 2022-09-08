package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;

import lombok.Data;

/**
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=5
 * @author ChenHailong
 * @date 2020/2/19 13:50
 **/
@Data
public class WXRequestPaymentParam implements Serializable {
    String timeStamp;
    String nonceStr;
    String prepayId;
    String signType;
    String paySign;
}
