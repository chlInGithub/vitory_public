package com.chl.victory.serviceapi.weixin.model.pay;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/12 16:46
 **/
@Data
public class PayBaseResult implements Serializable {

    /**
     * 返回状态码 SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     */
    String return_code;

    String return_msg;

    /*以下字段在return_code为SUCCESS的时候有返回 */

    /**
     * 调用接口提交的小程序ID
     */
    String appid;

    /**
     * 调用接口提交的商户号
     */
    String mch_id;

    /**
     * 子商户公众账号ID
     */
    String sub_appid;

    /**
     * 子商户号
     */
    String sub_mch_id;

    String device_info;

    /**
     * 随机字符串 微信返回的随机字符串
     */
    String nonce_str;

    /**
     * 微信返回的签名值
     */
    String sign;

    /**
     * 业务结果 SUCCESS/FAIL
     */
    String result_code;

    /**
     * 错误代码
     */
    String err_code;

    /**
     * 错误代码描述
     */
    String err_code_des;


    /* 以下字段在return_code 和result_code都为SUCCESS的时候有返回 */

    /**
     * 交易类型
     */
    String trade_type;


    public boolean isReturnSuccess() {
        return "SUCCESS".equals(return_code);
    }

    public boolean isResultSuccess() {
        return "SUCCESS".equals(result_code);
    }
}
