package com.chl.victory.serviceapi.activity;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.activity.model.ShopActivityDTO;
import com.chl.victory.serviceapi.activity.query.ShopActivityQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 15:54
 **/
public interface ShopActivityFacade {

    ServiceResult<Integer> count(ShopActivityQueryDTO query);

    ServiceResult saveActivity(ShopActivityDTO shopActivityDO, List<Long> itemIds);

    ServiceResult del(Long id, Long shopId);

    ServiceResult<List<ShopActivityDTO>> selectActivity(ShopActivityQueryDTO query);

    ServiceResult valid(Long id, Long shopId);

    ServiceResult invalid(Long id, Long shopId);
}
