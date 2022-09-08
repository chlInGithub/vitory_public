package com.chl.victory.serviceimpl.merchant;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.SaleStrategyFacade;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.saleStrategyService;

/**
 * @author ChenHailong
 * @date 2020/9/11 13:59
 **/
@DubboService
public class SaleStrategyFacadeImpl implements SaleStrategyFacade {

    @Override
    public ServiceResult<Integer> count(SaleStrategyDTO query) {
        return saleStrategyService.count(query);
    }

    @Override
    public ServiceResult save(SaleStrategyDTO dto, List<Long> itemIds) {
        return saleStrategyService.save(dto, itemIds);
    }

    @Override
    public ServiceResult del(Long id, Long shopId) {
        return saleStrategyService.del(id, shopId);
    }

    @Override
    public ServiceResult<List<SaleStrategyDTO>> selectSaleStrategy(SaleStrategyDTO query) {
        return saleStrategyService.select(query);
    }
}
