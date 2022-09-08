package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;
import javax.persistence.*;

@Table(name = "`shop_app`")
public class ShopAppDO extends BaseDO {

    /**
     * 使用域名
     */
    String domain;
    /**
     * 小程序 公众号 appId
     * app_id
     */
    private String appId;

    /**
     * app_secret
     */
    private String appSecret;

    /**
     * 平台类型
     * third_type
     * @see  com.chl.victory.beans.enums.ThirdPlatformEnum
     */
    private Integer thirdType;

    /**
     * 刷新token
     * auth_refresh_token
     */
    private String authRefreshToken;

    /**
     * 支付配置
     * pay_config
     */
    private String payConfig;

    /**
     * 小程序样式配置
     */
    private String style;

    public Integer getFastRegiste() {
        return fastRegiste;
    }

    public void setFastRegiste(Integer fastRegiste) {
        this.fastRegiste = fastRegiste;
    }

    /**
     * 是否快速注册 1是 0不是
     */
    private Integer fastRegiste;

    public String getWxPayCert() {
        return wxPayCert;
    }

    public void setWxPayCert(String wxPayCert) {
        this.wxPayCert = wxPayCert;
    }

    private String wxPayCert;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret == null ? null : appSecret.trim();
    }

    public Integer getThirdType() {
        return thirdType;
    }

    public void setThirdType(Integer thirdType) {
        this.thirdType = thirdType;
    }

    public String getAuthRefreshToken() {
        return authRefreshToken;
    }

    public void setAuthRefreshToken(String authRefreshToken) {
        this.authRefreshToken = authRefreshToken == null ? null : authRefreshToken.trim();
    }

    public String getPayConfig() {
        return payConfig;
    }

    public void setPayConfig(String payConfig) {
        this.payConfig = payConfig == null ? null : payConfig.trim();
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style == null ? null : style.trim();
    }
}