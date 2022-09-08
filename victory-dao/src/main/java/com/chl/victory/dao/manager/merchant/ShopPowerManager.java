package com.chl.victory.dao.manager.merchant;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.dao.mapper.merchant.ShopPowerMapper;
import com.chl.victory.dao.model.merchant.ShopPowerDO;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author ChenHailong
 * @date 2020/8/19 17:00
 **/
@Component
public class ShopPowerManager {
    @Resource
    ShopPowerMapper shopPowerMapper;

    public ShopPowerDO select(@NotNull Long shopId, @NotNull Byte powerType) {
        ShopPowerDO query = new ShopPowerDO();
        query.setShopId(shopId);
        query.setType(powerType);
        ShopPowerDO shopPowerDO = shopPowerMapper.selectOne(query);
        return shopPowerDO;
    }

    public List<ShopPowerDO> select(@NotNull Long shopId) {
        ShopPowerDO query = new ShopPowerDO();
        query.setShopId(shopId);
        List<ShopPowerDO> shopPowerDOs = shopPowerMapper.select(query);
        return shopPowerDOs;
    }

    public boolean valid(@NotNull Long shopId, @NotNull Byte powerType) {
        Example example = new Example(ShopPowerDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("shopId", shopId);
        criteria.andEqualTo("type", powerType);
        criteria.andGreaterThan("expiredTime", new Date());
        int count = shopPowerMapper.selectCountByExample(example);
        return count > 0;
    }
}
