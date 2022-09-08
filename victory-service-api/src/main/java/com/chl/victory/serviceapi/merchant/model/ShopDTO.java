package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopDTO extends BaseDTO  implements Serializable {

    /**
     * 店铺名称
     * name
     */
    private String name;

    /**
     * 店铺联系电话，用于发送店铺相关短信
     * mobile
     */
    private Long mobile;

    /**
     * 店铺图片，间隔符|
     * img
     */
    private String img;

    /**
     * 修改验证码
     */
    private String checkCode;

    /**
     * 用于update
     */
    private String nextCheckCode;

    private String address;

    // private String domain;

    /**
     * 店铺失效时间
     */
    private Date expiredTime;

    /**
     * 免邮对订单金额的要求
     */
    private BigDecimal freightFree;

    private String deliveryArea;

    public static List<Integer> parsePayType( String payType) {
        return payType == null || payType.trim().equals("") ? null : JSONObject.parseArray(payType, Integer.class);
    }

    private String payType;

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setImg(String img) {
        this.img = img == null ? null : img.trim();
    }
}