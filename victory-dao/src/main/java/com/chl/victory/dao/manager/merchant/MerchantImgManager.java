package com.chl.victory.dao.manager.merchant;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.merchant.ShopImgMapper;
import com.chl.victory.dao.mapper.merchant.ShopImgTotalMapper;
import com.chl.victory.dao.model.BaseDO;
import com.chl.victory.dao.model.merchant.ShopImgDO;
import com.chl.victory.dao.model.merchant.ShopImgTotalDO;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ChenHailong
 * @date 2019/11/20 13:29
 **/
@Component
public class MerchantImgManager extends BaseManager4TkMybatis {
    @Resource
    ShopImgMapper shopImgMapper;
    @Resource
    ShopImgTotalMapper shopImgTotalMapper;

    public int countImg(ShopImgDO query) throws DaoManagerException {
        return count(shopImgMapper, query);
    }

    public int saveImg(ShopImgDO model) throws DaoManagerException {
        ShopImgDO checkExistOneQuery = new ShopImgDO();
        checkExistOneQuery.setShopId(model.getShopId());
        checkExistOneQuery.setImgId(model.getImgId());
        int col = save(shopImgMapper, model, null, checkExistOneQuery, TableNameEnum.SHOP_IMG);
        return col;
    }

    public ShopImgDO selectImg(ShopImgDO query) throws DaoManagerException {
        return selectOne(shopImgMapper, query);
    }

    /**
     * @param query
     * @param pageIndex based-0
     * @param pageSize
     * @return
     */
    public List<ShopImgDO> selectImgs(ShopImgDO query, int pageIndex, int pageSize) {
        RowBounds rowBounds = new RowBounds(pageIndex * pageSize, pageSize);
        return shopImgMapper.selectByRowBounds(query, rowBounds);
    }

    public int delImg(ShopImgDO query) throws DaoManagerException {
        int col = del(shopImgMapper, query);
        return col;
    }

    public ShopImgTotalDO selectImgTotal(ShopImgTotalDO query) throws DaoManagerException {
        return selectOne(shopImgTotalMapper, query);
    }

    public int saveImgTotal(ShopImgTotalDO model) throws DaoManagerException {
        ShopImgTotalDO checkExistQuery = new ShopImgTotalDO();
        checkExistQuery.setShopId(model.getShopId());
        int col = save(shopImgTotalMapper, model, checkExistQuery, checkExistQuery, TableNameEnum.SHOP_IMG_TOTAL);
        return col;
    }

    public List<ShopImgDO> query4ImgMan(ShopImgDO shopImgDO) {
        return shopImgMapper.query4ImgMan(shopImgDO);
    }

    public Integer selectTotalSize(Long shopId) {
        return shopImgTotalMapper.totoalSize(shopId);
    }
}
