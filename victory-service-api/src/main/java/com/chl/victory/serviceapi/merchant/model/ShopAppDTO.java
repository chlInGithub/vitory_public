package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopAppDTO extends BaseDTO implements Serializable {

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
     * @see ThirdPlatformEnum
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
     * 是否快速注册 1是 0不是
     */
    private Integer fastRegiste;

    private String wxPayCert;

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret == null ? null : appSecret.trim();
    }

    public void setAuthRefreshToken(String authRefreshToken) {
        this.authRefreshToken = authRefreshToken == null ? null : authRefreshToken.trim();
    }

    public void setPayConfig(String payConfig) {
        this.payConfig = payConfig == null ? null : payConfig.trim();
    }
}