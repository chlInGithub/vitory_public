package com.chl.victory.serviceimpl.activity;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.ShopActivityFacade;
import com.chl.victory.serviceapi.activity.model.ShopActivityDTO;
import com.chl.victory.serviceapi.activity.query.ShopActivityQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.shopActivityService;

/**
 * @author ChenHailong
 * @date 2020/9/1 14:20
 **/
@DubboService
public class ShopActivityFacadeImpl implements ShopActivityFacade {

    @Override
    public ServiceResult<Integer> count(ShopActivityQueryDTO query) {
        return shopActivityService.count(query);
    }

    @Override
    public ServiceResult saveActivity(ShopActivityDTO shopActivity, List<Long> itemIds) {
        return shopActivityService.saveActivity(shopActivity, itemIds);
    }

    @Override
    public ServiceResult del(Long id, Long shopId) {
        return shopActivityService.del(id, shopId);
    }

    @Override
    public ServiceResult<List<ShopActivityDTO>> selectActivity(ShopActivityQueryDTO query) {
        return shopActivityService.selectActivity(query);
    }

    @Override
    public ServiceResult valid(Long id, Long shopId) {
        return shopActivityService.valid(id, shopId);
    }

    @Override
    public ServiceResult invalid(Long id, Long shopId) {
        return shopActivityService.invalid(id, shopId);
    }
}
