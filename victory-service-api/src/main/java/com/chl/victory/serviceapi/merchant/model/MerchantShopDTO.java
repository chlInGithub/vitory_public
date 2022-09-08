package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class MerchantShopDTO extends BaseDTO implements Serializable {
    /**
     * 店主用户ID
     * merchant_id
     */
    private Long merchantId;
}