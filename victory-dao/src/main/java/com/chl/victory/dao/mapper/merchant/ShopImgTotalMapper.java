package com.chl.victory.dao.mapper.merchant;

import com.chl.victory.dao.mapper.BaseMapper4TkMybatis;
import com.chl.victory.dao.model.merchant.ShopImgTotalDO;

public interface ShopImgTotalMapper extends BaseMapper4TkMybatis<ShopImgTotalDO> {

    Integer totoalSize(Long shopId);
}