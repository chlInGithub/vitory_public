package com.chl.victory.serviceapi.merchant;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.ShopImgDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 17:09
 **/
public interface MerchantImgFacade {

    ServiceResult<Integer> totalSizeImg(Long shopId);

    ServiceResult saveImg(@NotNull ShopImgDTO dto);

    ServiceResult<Integer> countImg(Long shopId, String imgId);

    ServiceResult getUsedNum(@NotNull Long shopId, @NotEmpty String imgId);

    ServiceResult delOnlyImg(@NotNull Long shopId, @NotEmpty String imgId);

    ServiceResult delImg(@NotNull Long shopId, @NotEmpty String imgId);

    ServiceResult<List<ShopImgDTO>> queryImgs(Long shopId, String imgId);

    ServiceResult<List<ShopImgDTO>> query4ImgMan(Long shopId, Long id);
}
