package com.chl.victory.serviceimpl.item;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.item.ItemFacade;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.CategoryQueryDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.itemService;

/**
 * @author ChenHailong
 * @date 2020/9/1 17:17
 **/
@DubboService
public class ItemFacadeImpl implements ItemFacade {

    @Override
    public ServiceResult delCate(@NotNull Long shopId, @NotNull Long cateId) {
        return itemService.delCate(shopId, cateId);
    }

    @Override
    public ServiceResult modifyCateShow(@NotNull Long shopId, @NotNull Long cateId, @NotNull Integer show) {
        return itemService.modifyCateShow(shopId, cateId, show);
    }

    @Override
    public ServiceResult<List<CategoryDTO>> selectCates(@NotNull CategoryQueryDTO query) {
        return itemService.selectCates(query);
    }

    @Override
    public ServiceResult saveCate(@NotNull CategoryDTO model) {
        return itemService.saveCate(model);
    }

    @Override
    public ServiceResult<Integer> countCate(@NotNull CategoryQueryDTO query) {
        return itemService.countCate(query);
    }

    @Override
    public String getItemSummary(@NotNull Long shopId) {
        return itemService.getItemSummary(shopId);
    }

    @Override
    public ServiceResult<List<ItemDTO>> selectItems(@NotNull ItemQueryDTO query) {
        return itemService.selectItems(query);
    }

    @Override
    public ServiceResult<Integer> countItem(@NotNull ItemQueryDTO query) {
        return itemService.countItem(query);
    }

    @Override
    public ServiceResult save(@NotNull ItemDTO itemDTO, List<SkuDTO> skuDTOS) {
        return itemService.save(itemDTO, skuDTOS);
    }

    @Override
    public ServiceResult updateStatus(@NotNull @Positive Long itemId, @NotNull @Positive Integer status,
            @NotNull @Positive Long shopId) {
        return itemService.updateStatus(itemId, status, shopId);
    }

    @Override
    public ServiceResult delItem(@NotNull Long shopId, @NotEmpty List<Long> itemIds) {
        return itemService.delItem(shopId, itemIds);
    }

    @Override
    public ServiceResult shelf(Long shopId, Long itemId) {
        return itemService.shelf(shopId, itemId);
    }

    @Override
    public ServiceResult soldOut(Long shopId, Long itemId) {
        return itemService.soldOut(shopId, itemId);
    }

    @Override
    public ServiceResult<List<SkuDTO>> selectSkus(@NotNull SkuQueryDTO query) {
        return itemService.selectSkus(query);
    }

    @Override
    public List<Long> selectAllLeafCateIds(Long cateId, Long shopId) {
        return itemService.selectAllLeafCateIds(cateId, shopId);
    }

    @Override
    public List<Long> selectAllCachedLeafCateIds(Long cateId, Long shopId) {
        return itemService.selectAllCachedLeafCateIds(cateId, shopId);
    }
}
