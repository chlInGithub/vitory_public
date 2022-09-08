package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/29 15:55
 **/
@Data
public class StyleConfigDTO implements Serializable {

    /**
     * 导航栏背景色
     */
    String navBarBackColor;

    /**
     * 导航栏文字颜色
     */
    String navBarFontColor;

    /**
     * 页面背景色
     */
    String bgColor;

    public static StyleConfigDTO parse(String json){
        StyleConfigDTO payConfig = JSONObject.parseObject(json, StyleConfigDTO.class);
        return payConfig;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
