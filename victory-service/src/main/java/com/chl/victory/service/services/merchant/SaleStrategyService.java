package com.chl.victory.service.services.merchant;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.merchant.SaleStrategyManager;
import com.chl.victory.dao.model.merchant.SaleStrategyDO;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.common.enums.merchant.SaleStrategyTypeEnum;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.localservice.manager.LocalServiceManager.saleStrategyLocalService;

/**
 * @author ChenHailong
 * @date 2019/5/9 11:05
 **/
@Service
@Validated
public class SaleStrategyService extends BaseService {

    @Resource
    SaleStrategyManager saleStrategyManager;

    public ServiceResult save(@NotNull SaleStrategyDTO dto, List<Long> itemIds) {
        try {
            SaleStrategyDO saleStrategyDO = toDO(dto, SaleStrategyDO.class);
            int saveCount = saleStrategyManager.save(saleStrategyDO);
            if (saveCount > 0) {
                Long shopId = saleStrategyDO.getShopId();
                Long id = saleStrategyDO.getId();
                SaleStrategyDTO temp = new SaleStrategyDTO();
                temp.setStrategyType(dto.getStrategyType());
                temp.setAttr(dto.getAttr());
                temp.setPayType(dto.getPayType());
                saleStrategyLocalService
                        .saveStrategyCache(shopId, id, itemIds, SaleStrategyTypeEnum.getByCode(dto.getStrategyType()).getDealPoint(), JSONObject.toJSONString(temp));
            }
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<SaleStrategyDTO>> select(@NotNull SaleStrategyDTO queryDTO) {
        try {
            SaleStrategyDO query = toDO(queryDTO, SaleStrategyDO.class);
            List<SaleStrategyDO> shopActivityDOS = saleStrategyManager.select(query);
            List<SaleStrategyDTO> shopActivityDTOS = toDTOs(shopActivityDOS, SaleStrategyDTO.class);
            return ServiceResult.success(shopActivityDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
    public ServiceResult<List<SaleStrategyDTO>> select(@NotNull SaleStrategyDTO queryDTO, @NotNull Integer pageIndex, @NotNull Integer pageSize) {
        try {
            SaleStrategyDO query = toDO(queryDTO, SaleStrategyDO.class);
            List<SaleStrategyDO> shopActivityDOS = saleStrategyManager.select(query, pageIndex * pageSize, pageSize);
            List<SaleStrategyDTO> shopActivityDTOS = toDTOs(shopActivityDOS, SaleStrategyDTO.class);
            return ServiceResult.success(shopActivityDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<Integer> count(@NotNull SaleStrategyDTO queryDTO) {
        try {
            SaleStrategyDO query = toDO(queryDTO, SaleStrategyDO.class);
            return ServiceResult.success(saleStrategyManager.count(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult del(Long id, Long shopId) {
        SaleStrategyDO query = new SaleStrategyDO();
        query.setShopId(shopId);
        query.setId(id);
        Integer dealPoint = null;
        try {
            List<SaleStrategyDO> saleStrategyDOS = saleStrategyManager.select(query);
            if (!CollectionUtils.isEmpty(saleStrategyDOS)) {
                SaleStrategyDO saleStrategyDO = saleStrategyDOS.get(0);
                dealPoint = SaleStrategyTypeEnum.getByCode(saleStrategyDO.getStrategyType()).getDealPoint();
            }
            saleStrategyManager.del(query);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        saleStrategyLocalService.delStrategyCache(shopId, id, dealPoint);

        return ServiceResult.success();
    }
}
