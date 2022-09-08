package com.chl.victory.dao.mapper.merchant;

import java.util.List;

import com.chl.victory.dao.mapper.BaseMapper4TkMybatis;
import com.chl.victory.dao.model.merchant.ShopImgDO;
import org.apache.ibatis.annotations.Param;

public interface ShopImgMapper extends BaseMapper4TkMybatis<ShopImgDO> {
    List<ShopImgDO> query4ImgMan(ShopImgDO shopImgDO);
}