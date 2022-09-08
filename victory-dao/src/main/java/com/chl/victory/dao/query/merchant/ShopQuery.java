package com.chl.victory.dao.query.merchant;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

@Data
public class ShopQuery extends BaseQuery {
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

    private String checkCode;

    @Override
    public void setShopId(Long shopId){
        setId(shopId);
    }
}