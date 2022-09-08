package com.chl.victory.service.services.merchant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.merchant.MerchantImgManager;
import com.chl.victory.dao.model.merchant.ShopImgDO;
import com.chl.victory.dao.model.merchant.ShopImgTotalDO;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.ShopImgDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @author ChenHailong
 * @date 2019/11/20 14:16
 **/
@Service
@Validated
public class MerchantImgService extends BaseService {

    @Resource
    MerchantImgManager merchantImgManager;

    public ServiceResult<Integer> getUsedNum(@NotNull Long shopId, @NotEmpty String imgId) {
        ShopImgDO shopImgDO = new ShopImgDO();
        shopImgDO.setShopId(shopId);
        shopImgDO.setImgId(imgId);
        ShopImgDO result;
        try {
            result = merchantImgManager.selectImg(shopImgDO);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        if (result != null) {
            return ServiceResult.success(result.getUsedNum());
        }
        return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST);
    }

    /**
     * 仅仅删除 店铺-图片 关系
     * @param shopId
     * @param imgId
     * @return
     */
    public ServiceResult delOnlyImg(@NotNull Long shopId, @NotEmpty String imgId) {
        ShopImgDO shopImgDO = new ShopImgDO();
        shopImgDO.setShopId(shopId);
        shopImgDO.setImgId(imgId);
        try {
            // 存在？
            ShopImgDO shopImgExistModel = merchantImgManager.selectImg(shopImgDO);
            if (null == shopImgExistModel) {
                return ServiceResult.success();
            }

            // 图片仅被一处引用
            if (shopImgExistModel.getUsedNum() <= 1) {
                int delReuslt = merchantImgManager.delImg(shopImgDO);
                if (delReuslt > 0) {
                    return ServiceResult.success();
                }
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
            }

            // 图片被多处使用
            shopImgExistModel.setUsedNum(shopImgExistModel.getUsedNum() - 1);
            shopImgExistModel.setCreatedTime(null);
            shopImgExistModel.setModifiedTime(null);
            int saveImgResult = merchantImgManager.saveImg(shopImgExistModel);
            if (saveImgResult > 0) {
                return ServiceResult.success();
            }
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 删除店铺图片关系 和 减少店铺图片总大小
     * @param shopId
     * @param imgId
     * @return
     */
    public ServiceResult delImg(@NotNull Long shopId, @NotEmpty String imgId) {
        final ShopImgDO shopImgQuery = new ShopImgDO();
        shopImgQuery.setShopId(shopId);
        shopImgQuery.setImgId(imgId);
        ShopImgDO existShopImg;
        final ShopImgTotalDO shopImgTotalDO = new ShopImgTotalDO();
        try {
            // 判断关系存在
            existShopImg = merchantImgManager.selectImg(shopImgQuery);
            if (null == existShopImg) {
                return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST);
            }

            ShopImgTotalDO shopImgTotalQuery = new ShopImgTotalDO();
            shopImgTotalQuery.setShopId(shopId);
            ShopImgTotalDO shopImgTotalResult = merchantImgManager.selectImgTotal(shopImgTotalQuery);
            if (null == shopImgTotalResult) {
                return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST);
            }
            BeanUtils.copyProperties(shopImgTotalResult, shopImgTotalDO);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        // 减少店铺图片总大小
        shopImgTotalDO.setSize(shopImgTotalDO.getSize() - existShopImg.getSize());
        shopImgTotalDO.setCreatedTime(null);
        shopImgTotalDO.setModifiedTime(null);

        return transactionTemplate.execute(status -> {
            try {
                int saveResult = merchantImgManager.saveImgTotal(shopImgTotalDO);
                if (saveResult < 0) {
                    return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
                }

                int delResult = merchantImgManager.delImg(shopImgQuery);
                if (delResult < 0) {
                    status.setRollbackOnly();
                    return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
                }

                return ServiceResult.success(true);
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        });
    }

    public ServiceResult<Integer> countImg(Long shopId, String imgId) {
        ShopImgDO query = new ShopImgDO();
        query.setShopId(shopId);
        query.setImgId(imgId);
        try {
            int count = merchantImgManager.countImg(query);
            return ServiceResult.success(count);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 店铺图片占用空间大小
     * @param shopId
     * @return
     */
    public ServiceResult<Integer> totalSizeImg(Long shopId) {
        Integer total = merchantImgManager.selectTotalSize(shopId);
        return ServiceResult.success(total);
    }

    public ServiceResult saveImg(@NotNull ShopImgDTO dto) {
        ValidationUtil.validate(dto);
        ShopImgDO query = new ShopImgDO();
        query.setShopId(dto.getShopId());
        query.setImgId(dto.getImgId());
        return transactionTemplate.execute(status -> {
            try {
                ShopImgDO shopImgDO = merchantImgManager.selectImg(query);
                // 已经存在shop-imgid关系，则应用+1
                if (null != shopImgDO) {
                    shopImgDO.setUsedNum(shopImgDO.getUsedNum() + 1);
                    shopImgDO.setCreatedTime(null);
                    shopImgDO.setModifiedTime(null);
                    int saveResult = merchantImgManager.saveImg(shopImgDO);
                    if (saveResult < 1) {
                        return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "处理店铺图片关系失败");
                    }
                    return ServiceResult.success();
                }

                // 保存 店铺-图片 关系
                shopImgDO = new ShopImgDO();
                BeanUtils.copyProperties(dto, shopImgDO);
                shopImgDO.setUsedNum(1);
                int saveImgResult = merchantImgManager.saveImg(shopImgDO);

                if (saveImgResult < 1) {
                    return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "保存店铺图片关系失败");
                }

                // 增加店铺已上传图片总大小
                Long totalSize = 0L;
                ShopImgTotalDO shopImgTotalDO = new ShopImgTotalDO();
                shopImgTotalDO.setShopId(shopImgDO.getShopId());
                ShopImgTotalDO shopImgTotalDOResult = merchantImgManager.selectImgTotal(shopImgTotalDO);
                if (null != shopImgTotalDOResult) {
                    totalSize = shopImgTotalDOResult.getSize();
                    shopImgTotalDO.setId(shopImgTotalDOResult.getId());
                }
                totalSize += shopImgDO.getSize();
                shopImgTotalDO.setSize(totalSize);
                shopImgTotalDO.setOperatorId(shopImgDO.getOperatorId());
                int saveImgTotalResult = merchantImgManager.saveImgTotal(shopImgTotalDO);
                if (saveImgTotalResult < 1) {
                    status.setRollbackOnly();
                    return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "保存店铺图片总大小失败");
                }

                return ServiceResult.success();
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        });
    }

    /**
     * 用于图片管理页面，查询ID小于id的limit张图片
     * @param id
     * @return
     */
    public ServiceResult<List<ShopImgDTO>> query4ImgMan(Long shopId, Long id) {
        ShopImgDO shopImgDO = new ShopImgDO();
        shopImgDO.setShopId(shopId);
        shopImgDO.setId(id);
        List<ShopImgDO> imgDOS = merchantImgManager.query4ImgMan(shopImgDO);
        List<ShopImgDTO> imgDTOS = Collections.EMPTY_LIST;
        if (!CollectionUtils.isEmpty(imgDOS)) {
            imgDTOS = imgDOS.stream().map(item -> {
                ShopImgDTO shopImgDTO = new ShopImgDTO();
                BeanUtils.copyProperties(item, shopImgDTO);
                return shopImgDTO;
            }).collect(Collectors.toList());
        }
        return ServiceResult.success(imgDTOS);
    }

    /**
     * 查询图片
     * @param shopId
     * @param imgId
     * @return
     */
    public ServiceResult<List<ShopImgDTO>> queryImgs(Long shopId, String imgId) {
        ShopImgDO shopImgDO = new ShopImgDO();
        shopImgDO.setShopId(shopId);
        shopImgDO.setImgId(imgId);
        List<ShopImgDO> imgDOs;
        try {
            imgDOs = merchantImgManager.selectImgs(shopImgDO, 0, 100);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        List<ShopImgDTO> dtos = Collections.EMPTY_LIST;
        if (!CollectionUtils.isEmpty(imgDOs)) {
            dtos = imgDOs.stream().map(item -> {
                ShopImgDTO shopImgDTO = new ShopImgDTO();
                BeanUtils.copyProperties(item, shopImgDTO);
                return shopImgDTO;
            }).collect(Collectors.toList());
        }

        return ServiceResult.success(dtos);
    }
}
