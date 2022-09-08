package com.chl.victory.dao.mapper.merchant;

import com.chl.victory.dao.mapper.BaseMapper4TkMybatis;
import com.chl.victory.dao.model.merchant.ShopAppDO;

public interface ShopAppMapper extends BaseMapper4TkMybatis<ShopAppDO> {

    String selectStyle(ShopAppDO query);

    int updateStyle(ShopAppDO shopAppDO);

    String selectWXPayCert(ShopAppDO query);

    int updateWXPayCert(ShopAppDO shopAppDO);

    int countWXPayCert(ShopAppDO shopAppDO);
}