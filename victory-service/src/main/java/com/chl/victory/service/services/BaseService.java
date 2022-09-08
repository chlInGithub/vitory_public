package com.chl.victory.service.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.dao.model.BaseDO;
import com.chl.victory.service.utils.BeanUtils;
import com.chl.victory.serviceapi.BaseDTO;
import com.chl.victory.serviceapi.BaseQuery;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

/**
 * 业务baseService
 * <ul>
 * <li>
 * 当方法中存在多表数据更改时，须使用事务
 * </li>
 * </ul>
 * @author ChenHailong
 * @date 2019/5/8 9:34
 **/
public class BaseService {

    @Resource
    protected TransactionTemplate transactionTemplate;

    /**
     * @param e
     * @return 追溯最多4层ex，返回满足长度要求的异常描述
     */
    protected String trimExMsg(Throwable e) {
        return ExceptionUtil.trimExMsg(e);
    }

    protected <T extends BaseDTO, D extends BaseDO> List<D> toDOs(List<T> models, Class<D> clazz) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.EMPTY_LIST;
        }
        List<D> dtos = models.stream().map(item -> toDO(item, clazz)).collect(Collectors.toList());
        return dtos;
    }
    protected <T extends BaseDTO, D extends BaseDO> List<T> toDTOs(List<D> models, Class<T> clazz) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.EMPTY_LIST;
        }
        List<T> dtos = models.stream().map(item -> toDTO(item, clazz)).collect(Collectors.toList());
        return dtos;
    }

    protected <T extends BaseDTO, D extends BaseDO> T toDTO(@NotNull D model, Class<T> clazz) {
        return BeanUtils.copyProperties(model, clazz);
    }

    protected <T extends BaseDTO, D extends BaseDO> D toDO(@NotNull T dto, Class<D> clazz) {
        return BeanUtils.copyProperties(dto, clazz);
    }

    protected <T extends BaseQuery, D extends com.chl.victory.dao.query.BaseQuery> D toQuery(@NotNull T queryDto,
            Class<D> clazz) {
        return BeanUtils.copyProperties(queryDto, clazz);
    }
}
