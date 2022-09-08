package com.chl.victory.dao.query.merchant;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

@Data
public class MerchantShopQuery extends BaseQuery {
    /**
     * 店主用户ID
     * merchant_id
     */
    private Long merchantId;
}