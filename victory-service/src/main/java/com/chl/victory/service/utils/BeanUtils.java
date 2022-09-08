package com.chl.victory.service.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/9/2 13:49
 **/
public class BeanUtils {
    public static <T, D> T copyProperties(@NonNull D model, Class<T> clazz) {
        if (null == model) {
            return null;
        }
        T dto = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        org.springframework.beans.BeanUtils.copyProperties(model, dto);
        return dto;
    }

    public static  <T, D> List<D> copyList(List<T> models, Class<D> clazz) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.EMPTY_LIST;
        }
        List<D> dtos = models.stream().map(item -> copyProperties(item, clazz)).collect(Collectors.toList());
        return dtos;
    }
}
