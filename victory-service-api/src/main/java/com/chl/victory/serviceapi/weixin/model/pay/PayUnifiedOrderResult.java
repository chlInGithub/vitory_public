package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * 微信支付 统一下单 返回结果的数据模型 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1&index=1
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class PayUnifiedOrderResult extends PayBaseResult implements Serializable {

    /**
     * 子商户公众账号ID
     */
    String sub_appid;

    /**
     * 子商户号
     */
    String sub_mch_id;

    /**
     * 预支付交易会话标识
     */
    String prepay_id;

    /**
     * 二维码链接
     */
    String code_url;

}
