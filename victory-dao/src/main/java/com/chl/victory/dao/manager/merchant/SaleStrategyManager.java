package com.chl.victory.dao.manager.merchant;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.enums.merchant.SaleStrategyTypeEnum;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.merchant.SaleStrategyMapper;
import com.chl.victory.dao.model.merchant.SaleStrategyDO;
import org.springframework.stereotype.Component;

/**
 * 销售策略数据层访问入口
 * @author ChenHailong
 * @date 2019/4/30 14:30
 **/
@Component
public class SaleStrategyManager extends BaseManager4TkMybatis {

    @Resource
    SaleStrategyMapper saleStrategyMapper;

    public int save(SaleStrategyDO model) throws DaoManagerException {
        if (model.getShopId() == null) {
            throw new DaoManagerException("缺少shopId");
        }

        SaleStrategyDO checkNotExist4Insert = null;
        SaleStrategyDO checkOnlyOne4Update = null;
        if (model.getId() == null) {
            if (SaleStrategyTypeEnum.preSale.getCode().equals(model.getStrategyType())) {
                checkNotExist4Insert = new SaleStrategyDO();
                checkNotExist4Insert.setShopId(model.getShopId());
                checkNotExist4Insert.setStrategyType(model.getStrategyType());
            }
        }
        else {
            checkOnlyOne4Update = new SaleStrategyDO();
            checkOnlyOne4Update.setId(model.getId());
            checkOnlyOne4Update.setShopId(model.getShopId());
        }

        return save(saleStrategyMapper, model, checkNotExist4Insert, checkOnlyOne4Update, TableNameEnum.SALE_STRATEGY);
    }

    public int del(SaleStrategyDO query) throws DaoManagerException {
        return del(saleStrategyMapper, query);
    }

    public List<SaleStrategyDO> select(SaleStrategyDO query) throws DaoManagerException {
        return select(saleStrategyMapper, query);
    }

    public List<SaleStrategyDO> select(@NotNull SaleStrategyDO query, @NotNull Integer pageIndex, @NotNull Integer pageSize) throws DaoManagerException {
        return select(saleStrategyMapper, query, pageIndex, pageSize);
    }

    public int count(SaleStrategyDO query) throws DaoManagerException {
        return count(saleStrategyMapper, query);
    }
}
