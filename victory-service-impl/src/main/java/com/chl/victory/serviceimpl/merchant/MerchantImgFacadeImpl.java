package com.chl.victory.serviceimpl.merchant;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.MerchantImgFacade;
import com.chl.victory.serviceapi.merchant.model.ShopImgDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.merchantImgService;

/**
 * @author ChenHailong
 * @date 2020/9/2 15:53
 **/
@DubboService
public class MerchantImgFacadeImpl implements MerchantImgFacade {

    @Override
    public ServiceResult<Integer> totalSizeImg(Long shopId) {
        return merchantImgService.totalSizeImg(shopId);
    }

    @Override
    public ServiceResult saveImg(@NotNull ShopImgDTO dto) {
        return merchantImgService.saveImg(dto);
    }

    @Override
    public ServiceResult<Integer> countImg(Long shopId, String imgId) {
        return merchantImgService.countImg(shopId, imgId);
    }

    @Override
    public ServiceResult<Integer> getUsedNum(@NotNull Long shopId, @NotEmpty String imgId) {
        return merchantImgService.getUsedNum(shopId, imgId);
    }

    @Override
    public ServiceResult delOnlyImg(@NotNull Long shopId, @NotEmpty String imgId) {
        return merchantImgService.delOnlyImg(shopId, imgId);
    }

    @Override
    public ServiceResult delImg(@NotNull Long shopId, @NotEmpty String imgId) {
        return merchantImgService.delImg(shopId, imgId);
    }

    @Override
    public ServiceResult<List<ShopImgDTO>> queryImgs(Long shopId, String imgId) {
        return merchantImgService.queryImgs(shopId, imgId);
    }

    @Override
    public ServiceResult<List<ShopImgDTO>> query4ImgMan(Long shopId, Long id) {
        return merchantImgService.query4ImgMan(shopId, id);
    }
}
