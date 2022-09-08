package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/29 15:55
 **/
@Data
public class PayConfigDTO implements Serializable {

    /**
     * 入驻为特约商户？
     */
    boolean sub;

    String mchId;

    String apiKey;

    public static PayConfigDTO parse(String json){
        PayConfigDTO payConfig = JSONObject.parseObject(json, PayConfigDTO.class);
        return payConfig;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
