package com.chl.victory.localservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/11/3 13:56
 **/
@Component
public class SmartCodeLocalService {

    @Resource
    CacheService cacheService;

    /**
     * 获取活码数据
     * @param userId
     * @param smartCodeId
     * @return
     */
    public SmartCode getSmartCode(@NotNull Long userId, @NotEmpty String smartCodeId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_MAP + userId;
        SmartCode smartCode = cacheService.hGet(key, smartCodeId, SmartCode.class);
        return smartCode;
    }
    public List<SmartCode> getSmartCodes(@NotNull Long userId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_MAP + userId;
        Map<String, SmartCode> stringSmartCodeMap = cacheService.hScan(key, String.class, SmartCode.class);
        return new ArrayList(stringSmartCodeMap.values());
    }
    public Long countSmartCodes(@NotNull Long userId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_MAP + userId;
        Long count = cacheService.hLen(key);
        return count;
    }

    /**
     * 子码选择规则：扫码次数没有达到上限 且 优先级级别高的 子码，若所有子码的扫码次数均达上限，则选择优先级别搞得子码；
     *
     * @param smartCodeId
     * @return
     */
    public SmartEle getSmartCodeGoodEle(@NotEmpty String smartCodeId) {
        String key = CacheKeyPrefix.SMART_CODE_MAP_USER;
        Long userId = cacheService.hGet(key, smartCodeId, Long.class);
        if (null == userId) {
            return null;
        }

        List<SmartEle> smartEles = getSmartEles(userId, smartCodeId);
        if (CollectionUtils.isEmpty(smartEles)) {
            return null;
        }

        /*for (SmartEle smartEle : smartEles) {
            Integer integer = getEleShowCount(smartCodeId, smartEle.getId());
            if (null != integer) {
                smartEle.setShowCount(integer);
            }
        }*/
        List<SmartEle> smartElesTemp = smartEles.stream().filter(smartEle -> smartEle.getShowMax() == null || smartEle.getShowMax() > smartEle.getShowCount())/*.sorted((o1, o2) -> o2.sort.compareTo(o1.sort))*/.collect(Collectors.toList());

        if (CollectionUtils.isEmpty(smartElesTemp)) {
            return smartEles.get(0);
        }

        SmartEle smartEle = smartElesTemp.get(0);

        return smartEle;
    }

    /**
     * 保存活码数据
     * @param userId
     * @param smartCode
     */
    public void saveSmartCode(@NotNull Long userId, @NotNull SmartCode smartCode) {
        String key = CacheKeyPrefix.SMART_CODE_USER_MAP + userId;
        String field = smartCode.getId();
        cacheService.hSet(key, field, smartCode);

        String key1 = CacheKeyPrefix.SMART_CODE_MAP_USER;
        String field1 = smartCode.getId();
        cacheService.hSet(key1, field, userId);
    }

    /**
     * 删除活码数据以及子元素
     * @param userId
     * @param smartCodeId
     */
    public void delSmartCode(@NotNull Long userId, @NotNull String smartCodeId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_MAP + userId;
        cacheService.hDel(key, smartCodeId);

        List<SmartEle> smartEles = getSmartEles(userId, smartCodeId);
        if (!CollectionUtils.isEmpty(smartEles)) {
            for (SmartEle smartEle : smartEles) {
                String field = smartCodeId + CacheKeyPrefix.SEPARATOR + smartEle.getId();
                cacheService.hDel(CacheKeyPrefix.SMART_CODE_ELE_COUNT_MAP, field);
            }
        }

        key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        cacheService.delKey(key);

        cacheService.hDel(CacheKeyPrefix.SMART_CODE_MAP_USER, smartCodeId);
    }

    /**
     * 获取活码的某个子元素
     * @param userId
     * @param smartCodeId
     * @param eleId
     * @return
     */
    public SmartEle getSmartEle(@NotNull Long userId, @NotNull String smartCodeId, @NotNull String eleId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        SmartEle ele = cacheService.hGet(key, eleId, SmartEle.class);
        if (null != ele) {
            Integer eleShowCount = getEleShowCount(smartCodeId, ele.getId());
            ele.setShowCount(eleShowCount);
        }
        return ele;
    }
    public Integer getEleShowCount(@NotNull String smartCodeId, @NotNull String eleId) {
        String key = CacheKeyPrefix.SMART_CODE_ELE_COUNT_MAP;
        String field = smartCodeId + CacheKeyPrefix.SEPARATOR + eleId;
        Integer count = cacheService.hGet(key, field, Integer.class);
        return count == null ? 0: count;
    }
    public void increEleShowCount(@NotNull String smartCodeId, @NotNull String eleId) {
        String key = CacheKeyPrefix.SMART_CODE_ELE_COUNT_MAP;
        String field = smartCodeId + CacheKeyPrefix.SEPARATOR + eleId;
        cacheService.hIncrement(key, field, 1);
    }

    public List<SmartEle> getSmartEles(@NotNull Long userId, @NotNull String smartCodeId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        Map<String, SmartEle> smartEleMap = cacheService.hScan(key, String.class, SmartEle.class);
        if (CollectionUtils.isEmpty(smartEleMap)) {
            return Collections.EMPTY_LIST;
        }
        List<SmartEle> smartEles = new ArrayList(smartEleMap.values());
        for (SmartEle ele : smartEles) {
            if (null != ele) {
                Integer eleShowCount = getEleShowCount(smartCodeId, ele.getId());
                ele.setShowCount(eleShowCount);
            }
        }
        smartEles.sort((o1, o2) -> o2.sort.compareTo(o1.sort));

        return smartEles;
    }

    public void saveSmartEle(@NotNull Long userId, @NotNull String smartCodeId, @NotNull SmartEle ele) {
        String key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        String field = ele.getId();
        cacheService.hSet(key, field, ele);
    }

    public void saveSmartEles(@NotNull Long userId, @NotNull String smartCodeId, @NotNull List<SmartEle> eles) {
        String key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        Map<String, SmartEle> smartEleMap = new HashMap<>();
        eles.forEach(smartEle -> smartEleMap.put(smartEle.getId(), smartEle));
        cacheService.hSet(key, smartEleMap);
    }

    public void delSmartEle(@NotNull Long userId, @NotNull String smartCodeId, @NotNull String eleId) {
        String key = CacheKeyPrefix.SMART_CODE_USER_ELE_MAP + userId + CacheKeyPrefix.SEPARATOR + smartCodeId;
        cacheService.hDel(key, eleId);
        String field = smartCodeId + CacheKeyPrefix.SEPARATOR + eleId;
        cacheService.hDel(CacheKeyPrefix.SMART_CODE_ELE_COUNT_MAP, field);
    }

    @Data
    public static class SmartCode {

        /**
         * 活码ID
         */
        String id;

        /**
         * 活码标题
         */
        String title;

        /**
         * 在码上增加图片md5，可表示图片归属
         */
        String ownerImg = "";

        /**
         * 活码图片md5
         */
        String codeImg;
    }

    @Data
    public static class SmartEle {

        String id;

        String title;

        /**
         * 图片md5
         */
        String img;

        /**
         * 优先级
         */
        Integer sort;

        /**
         * 展示次数
         */
        Integer showCount = 0;

        /**
         * 暂时次数上限
         */
        Integer showMax = 99999;
    }
}
