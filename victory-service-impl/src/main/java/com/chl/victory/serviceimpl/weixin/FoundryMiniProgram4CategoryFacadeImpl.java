package com.chl.victory.serviceimpl.weixin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4CategoryFacade;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.AddCategoryDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetCategoriesResult;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4CategoryService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2020/9/3 10:41
 **/
@DubboService
public class FoundryMiniProgram4CategoryFacadeImpl implements FoundryMiniProgram4CategoryFacade {

    @Override
    public ServiceResult<GetCategoriesResult> getCategory(@NotNull Long shopId, @NotEmpty String appId) {
        GetCategoriesResult category;
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            category = foundryMiniProgram4CategoryService.getCategory(weixinConfig);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(category);
    }

    @Override
    public ServiceResult<String> getAllCategories(@NotNull Long shopId, @NotEmpty String appId) {
        String allCategories;
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            allCategories = foundryMiniProgram4CategoryService.getAllCategories(weixinConfig);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(allCategories);
    }

    @Override
    public ServiceResult modifyCategory(@NotNull Long shopId, @NotEmpty String appId, @NotNull AddCategoryDTO addCategoryDTO) {
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4CategoryService.modifyCategory(weixinConfig, addCategoryDTO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addCategory(Long shopId, String appId, AddCategoryDTO addCategoryDTO) {
        try {
            ShopAppDO weixinConfig = merchantService.selectShopAppWithValidate(shopId, appId);
            foundryMiniProgram4CategoryService.addCategory(weixinConfig, addCategoryDTO);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success();
    }
}
