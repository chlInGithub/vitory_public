package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;

public class MerchantShopDO extends BaseDO {
    /**
     * 店主用户ID
     * merchant_id
     */
    private Long merchantId;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
}