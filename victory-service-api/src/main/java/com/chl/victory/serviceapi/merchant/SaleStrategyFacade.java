package com.chl.victory.serviceapi.merchant;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 15:54
 **/
public interface SaleStrategyFacade {

    ServiceResult<Integer> count(SaleStrategyDTO query);

    ServiceResult save(SaleStrategyDTO dto, List<Long> itemIds);

    ServiceResult del(Long id, Long shopId);

    ServiceResult<List<SaleStrategyDTO>> selectSaleStrategy(SaleStrategyDTO query);
}
