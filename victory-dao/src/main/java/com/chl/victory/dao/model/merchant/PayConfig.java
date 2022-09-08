package com.chl.victory.dao.model.merchant;

import javax.validation.constraints.NotEmpty;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/29 15:55
 **/
@Data
public class PayConfig {

    /**
     * 入驻为特约商户？
     */
    boolean sub;

    @NotEmpty(message = "缺少微信支付商户号")
    String mchId;

    String apiKey;

    public static PayConfig parse(String json){
        PayConfig payConfig = JSONObject.parseObject(json, PayConfig.class);
        return payConfig;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
