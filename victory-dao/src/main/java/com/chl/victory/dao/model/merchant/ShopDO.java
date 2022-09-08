package com.chl.victory.dao.model.merchant;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.dao.model.BaseDO;

public class ShopDO extends BaseDO {

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

    private String payType;

    private String deliveryType;

    public static List<Integer> parsePayType(String payType) {
        return payType == null || payType.trim().equals("") ? null : JSONObject.parseArray(payType, Integer.class);
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getDeliveryArea() {
        return deliveryArea;
    }

    public void setDeliveryArea(String deliveryArea) {
        this.deliveryArea = deliveryArea;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Long getMobile() {
        return mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img == null ? null : img.trim();
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getNextCheckCode() {
        return nextCheckCode;
    }

    public void setNextCheckCode(String nextCheckCode) {
        this.nextCheckCode = nextCheckCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getFreightFree() {
        return freightFree;
    }

    public void setFreightFree(BigDecimal freightFree) {
        this.freightFree = freightFree;
    }
}