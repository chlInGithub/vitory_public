package com.chl.victory.serviceapi.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.AddCategoryDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetCategoriesResult;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:14
 **/
public interface FoundryMiniProgram4CategoryFacade {

    ServiceResult<GetCategoriesResult> getCategory(@NotNull Long shopId, @NotEmpty String appId);
    ServiceResult<String> getAllCategories(@NotNull Long shopId, @NotEmpty String appId);
    ServiceResult modifyCategory(@NotNull Long shopId, @NotEmpty String appId, @NotNull AddCategoryDTO addCategoryDTO);

    ServiceResult addCategory(Long shopId, String appId, AddCategoryDTO addCategoryDTO);
}
