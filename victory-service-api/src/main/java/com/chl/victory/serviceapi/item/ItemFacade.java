package com.chl.victory.serviceapi.item;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.CategoryQueryDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:48
 **/
public interface ItemFacade {

    ServiceResult delCate(@NotNull Long shopId, @NotNull Long cateId);
    ServiceResult modifyCateShow(@NotNull Long shopId, @NotNull Long cateId, @NotNull Integer show);

    ServiceResult<List<CategoryDTO>> selectCates(@NotNull CategoryQueryDTO query);

    ServiceResult saveCate(@NotNull CategoryDTO model);

    ServiceResult<Integer> countCate(@NotNull CategoryQueryDTO query);

    String getItemSummary(@NotNull Long shopId);

    ServiceResult<List<ItemDTO>> selectItems(@NotNull ItemQueryDTO query);

    ServiceResult<Integer> countItem(@NotNull ItemQueryDTO query);

    ServiceResult save(@NotNull ItemDTO itemDO, List<SkuDTO> skuDOS);

    ServiceResult updateStatus(@NotNull @Positive Long itemId, @NotNull @Positive Integer status,
            @NotNull @Positive Long shopId);

    ServiceResult delItem(@NotNull Long shopId, @NotEmpty List<Long> itemIds);

    ServiceResult shelf(Long shopId, Long itemId);

    ServiceResult soldOut(Long shopId, Long itemId);

    ServiceResult<List<SkuDTO>> selectSkus(@NotNull SkuQueryDTO query);

    List<Long> selectAllLeafCateIds(Long cateId, Long shopId);

    List<Long> selectAllCachedLeafCateIds(Long cateId, Long shopId);

}
