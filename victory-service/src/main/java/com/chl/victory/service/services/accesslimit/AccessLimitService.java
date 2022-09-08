package com.chl.victory.service.services.accesslimit;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.accesslimit.AccessLimitManager;
import com.chl.victory.dao.model.accesslimit.AccessLimitDO;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author ChenHailong
 * @date 2020/4/10 14:31
 **/
@Service
@Slf4j
public class AccessLimitService {
    @Resource
    AccessLimitManager accessLimitManager;
    @Resource
    CacheService cacheService;

    public List<AccessLimitDO> select(AccessLimitDO query) {
        List<AccessLimitDO> accessLimitDOS = null;
        try {
            accessLimitDOS = accessLimitManager.select(query);
        } catch (DaoManagerException e) {
            log.error("AccessLimitService.select{}", query, e);
        }
        return accessLimitDOS;
    }

    public int save(AccessLimitDO model) {
        try {
            return accessLimitManager.save(model);
        } catch (DaoManagerException e) {
            log.error("AccessLimitService.save{}", model, e);
        }
        return -1;
    }


    final ConcurrentHashMap<String, AccessLimitGroup> accessLimitMap = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Object> updatingAccessLimitMap = new ConcurrentHashMap<>();

    @Data
    class AccessLimit {
        Long id;
        Long shopId;
        Integer type;
        Integer period;
        Integer maxLimit;
        Integer startTime;
        Integer endTime;
    }

    @Data
    @AllArgsConstructor
    class AccessLimitGroup{
        /**
         * 超时时间的毫秒形式
         */
        Long expireTime;

        List<AccessLimit> accessLimits;
    }

    /**
     * 获取限流配置
     * @param shopId
     * @param type
     * @return
     */
    AccessLimitGroup getAccessLimitGroup (Long shopId, Integer type) {
        String key = shopId + "_" + type;
        AccessLimitGroup accessLimitGroup = accessLimitMap.get(key);

        if (needRefresh(accessLimitGroup)) {
            // 针对同一主机 并发获取 时，避免多次重复读数据库
            Object lock = new Object();
            Object previons = updatingAccessLimitMap.putIfAbsent(key, lock);
            if (previons == null) {
                // 该线程负责更新
                synchronized (lock) {
                    log.info("accessLimit lock {} {}", lock, key);
                    // 从数据库取
                    AccessLimitDO query = new AccessLimitDO();
                    query.setShopId(shopId);
                    query.setType(type);
                    query.setInvalidTime(new Date());
                    List<AccessLimitDO> accessLimitDOS = select(query);

                    List<AccessLimit> accessLimits = null;
                    if (!CollectionUtils.isEmpty(accessLimitDOS)) {
                        accessLimits = accessLimitDOS.stream().filter(item-> item != null && (item.getPeriod() != null || item.getEndTime() != null)).map(item->{
                            AccessLimit accessLimit = new AccessLimit();
                            BeanUtils.copyProperties(item, accessLimit);
                            return accessLimit;
                        }).collect(Collectors.toList());

                    }

                    if (!CollectionUtils.isEmpty(accessLimits)) {
                        accessLimitGroup = new AccessLimitGroup(DateUtils.addMinutes(new Date(), 5).getTime(), accessLimits);
                    }else {
                        accessLimitGroup = new AccessLimitGroup(DateUtils.addMinutes(new Date(), 5).getTime(), null);
                    }
                    accessLimitMap.put(key, accessLimitGroup);

                    lock.notifyAll();
                }
            }else {
                synchronized (previons) {
                    accessLimitGroup = accessLimitMap.get(key);
                    if (needRefresh(accessLimitGroup)) {
                        log.info("accessLimit wait lock {} {}", previons, key);
                        try {
                            previons.wait(50);
                        } catch (InterruptedException e) {
                            log.warn("终止等待，获取限流 {} {}", previons, key);
                        }
                    }

                }
            }
            updatingAccessLimitMap.remove(key);
        }

        accessLimitGroup = accessLimitMap.get(key);

        return accessLimitGroup;
    }

    /**
     * 验证是否需要限流,未达到阈值则加1
     * 抛出异常AccessLimitException，表示超过限流阈值
     * @param shopId
     * @param typeEnum {@link com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum}
     * @param subfix 缓存key的后缀，用于表示粒度，如用户
     */
    public void doAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix, String errorMsg)
            throws AccessLimitException {
        AccessLimitGroup accessLimitGroup = getAccessLimitGroup(shopId, typeEnum.getCode());

        if (null == accessLimitGroup || CollectionUtils.isEmpty(accessLimitGroup.getAccessLimits())) {
            return;
        }

        for (AccessLimit accessLimit : accessLimitGroup.getAccessLimits()) {
            if (accessLimit.id == null) {
                continue;
            }
            if (accessLimit.period ==null && (accessLimit.endTime == null || accessLimit.startTime == null)) {
                continue;
            }

            Integer period = null;
            if (accessLimit.period != null) {
                period = accessLimit.period;
            }else {
                Integer currentSecondsOfToady = getCurrentSecondsOfToady();
                if (currentSecondsOfToady >= accessLimit.startTime && currentSecondsOfToady < accessLimit.endTime) {
                    period = accessLimit.endTime - currentSecondsOfToady;
                }
            }

            if (period == null || period < 1){
                continue;
            }

            String key = CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + accessLimit.id + (StringUtils
                    .isNotBlank(subfix) ? CacheKeyPrefix.SEPARATOR + subfix : "");
            doAccessLimit(key, accessLimit.maxLimit, period, errorMsg);
        }
    }

    /**
     * 验证是否需要限流
     * 抛出异常AccessLimitException，表示超过限流阈值
     * @param shopId
     * @param typeEnum {@link AccessLimitTypeEnum}
     * @param subfix 缓存key的后缀，用于表示粒度，如用户
     */
    public void checkAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix, String errorMsg)
            throws AccessLimitException {
        AccessLimitGroup accessLimitGroup = getAccessLimitGroup(shopId, typeEnum.getCode());

        if (null == accessLimitGroup || CollectionUtils.isEmpty(accessLimitGroup.getAccessLimits())) {
            return;
        }

        for (AccessLimit accessLimit : accessLimitGroup.getAccessLimits()) {
            if (accessLimit.id == null) {
                continue;
            }
            if (accessLimit.period ==null && (accessLimit.endTime == null || accessLimit.startTime == null)) {
                continue;
            }

            Integer period = null;
            if (accessLimit.period != null) {
                period = accessLimit.period;
            }else {
                Integer currentSecondsOfToady = getCurrentSecondsOfToady();
                if (currentSecondsOfToady >= accessLimit.startTime && currentSecondsOfToady < accessLimit.endTime) {
                    period = accessLimit.endTime - currentSecondsOfToady;
                }
            }

            if (period == null || period < 1){
                continue;
            }

            String key = CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + accessLimit.id + (StringUtils
                    .isNotBlank(subfix) ? CacheKeyPrefix.SEPARATOR + subfix : "");
            checkAccessLimit(key, accessLimit.maxLimit, period, errorMsg);
        }
    }

    /**
     * 增加限流数据
     * @param shopId
     * @param typeEnum
     * @param subfix
     */
    public void incrAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix) {
        AccessLimitGroup accessLimitGroup = getAccessLimitGroup(shopId, typeEnum.getCode());

        if (null == accessLimitGroup || CollectionUtils.isEmpty(accessLimitGroup.getAccessLimits())) {
            return;
        }

        for (AccessLimit accessLimit : accessLimitGroup.getAccessLimits()) {
            if (accessLimit.id == null) {
                continue;
            }

            if (accessLimit.period ==null && (accessLimit.endTime == null || accessLimit.startTime == null)) {
                continue;
            }

            Integer period = null;
            if (accessLimit.period != null) {
                period = accessLimit.period;
            }else {
                Integer currentSecondsOfToady = getCurrentSecondsOfToady();
                if (currentSecondsOfToady >= accessLimit.startTime && currentSecondsOfToady < accessLimit.endTime) {
                    period = accessLimit.endTime - currentSecondsOfToady;
                }
            }

            if (period == null || period < 1){
                continue;
            }

            String key = CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR + accessLimit.id + (StringUtils
                    .isNotBlank(subfix) ? CacheKeyPrefix.SEPARATOR + subfix : "");
            incrAccessLimit(key, period);
        }
    }

    private void doAccessLimit(){

    }

    /**
     * 当前数据量是否超过限流阈值，超过则异常。
     * @param shopId
     * @param current 当前数据量
     * @param typeEnum
     * @throws AccessLimitException
     */
    public void doAccessLimit(@NotNull Long shopId, @NotNull Integer current, @NotNull AccessLimitTypeEnum typeEnum, String errorMsg)
            throws AccessLimitException {
        AccessLimitGroup accessLimitGroup = getAccessLimitGroup(shopId, typeEnum.getCode());

        if (null == accessLimitGroup || CollectionUtils.isEmpty(accessLimitGroup.getAccessLimits())) {
            return;
        }

        for (AccessLimit accessLimit : accessLimitGroup.getAccessLimits()) {
            if (accessLimit.id == null) {
                continue;
            }

            if (current >= accessLimit.getMaxLimit()) {
                throw StringUtils.isBlank(errorMsg) ? new AccessLimitException() : new AccessLimitException(errorMsg);
            }
        }
    }

    /**
     * 检查是否超过阈值；超过，则异常；未超过，则+1。
     * @param key  cacheKey
     * @param max 阈值
     * @param period 周期
     * @throws AccessLimitException
     */
    public void doAccessLimit(String key, Integer max, Integer period, String errorMsg) throws AccessLimitException {
        boolean checkLimitAccess = cacheService
                .checkAndIncrLimitAccess(key, max, period);
        if (checkLimitAccess) {
            throw StringUtils.isBlank(errorMsg) ? new AccessLimitException() : new AccessLimitException(errorMsg);
        }
    }

    void incrAccessLimit(String key, Integer period) {
        cacheService.incr(key);
        cacheService.expire(key, period);
    }

    void checkAccessLimit(String key, Integer max, Integer period, String errorMsg) throws AccessLimitException {
        boolean checkLimitAccess = cacheService
                .checkLimitAccess(key, max, period);
        if (checkLimitAccess) {
            throw StringUtils.isBlank(errorMsg) ? new AccessLimitException() : new AccessLimitException(errorMsg);
        }
    }

    private Integer getCurrentSecondsOfToady() {
        Calendar calendar = Calendar.getInstance();
        Integer currentSesonds = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
        return currentSesonds;
    }

    /**
     * 是否需要更新本地cache
     * @return
     */
    private boolean needRefresh(AccessLimitGroup accessLimitGroup) {
        if (null == accessLimitGroup || accessLimitGroup.getExpireTime() == null) {
            return true;
        }

        if (accessLimitGroup.getExpireTime() <= System.currentTimeMillis()) {
            return true;
        }

        return false;
    }

}
