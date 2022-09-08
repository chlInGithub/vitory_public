package com.chl.victory.localservice;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

/**
 * 用户维度 活码的增值服务
 * @author ChenHailong
 * @date 2020/11/13 9:32
 **/
@Component
public class SmartCodePowerLocalService {
    @Resource
    CacheService cacheService;

    @Data
    @AllArgsConstructor
    public class SmartCodePower{
        SmartCodePowerEle codeCountLimit;
        SmartCodePowerEle eleCountLimit;
    }
    @Data
    @AllArgsConstructor
    public class SmartCodePowerEle{
        Integer max;
        Date expire;
    }

    /**
     * 默认 活码数量限制
     */
    private final SmartCodePowerEle defaultCodeCountPower = new SmartCodePowerEle(2, DateUtils.addYears(new Date(), 10));

    /**
     * 默认 活码中元素数量限制
     */
    private final SmartCodePowerEle defaultEleCountPower = new SmartCodePowerEle(2, DateUtils.addYears(new Date(), 10));

    /**
     * 默认 活码 限制
     */
    private final SmartCodePower defaultPower = new SmartCodePower(defaultCodeCountPower, defaultEleCountPower);

    /**
     * 获取增值服务，无则使用默认数据
     * @param userId
     * @return
     */
    public SmartCodePower getPower(@NotNull Long userId){
        String key = CacheKeyPrefix.SMART_CODE_POWER_MAP;
        String field = userId.toString();
        SmartCodePower smartCodePower = cacheService.hGet(key, field, SmartCodePower.class);

        // 能力超时 或 无数据
        boolean change = false;
        if (null == smartCodePower) {
            change = true;
            smartCodePower = defaultPower;
        }else {
            SmartCodePowerEle codeCountLimit = smartCodePower.getCodeCountLimit();
            if (null == codeCountLimit || codeCountLimit.getExpire().before(new Date())) {
                change = true;
                smartCodePower.setCodeCountLimit(defaultCodeCountPower);
            }

            SmartCodePowerEle eleCountLimit = smartCodePower.getEleCountLimit();
            if (null == eleCountLimit || eleCountLimit.getExpire().before(new Date())) {
                change = true;
                smartCodePower.setEleCountLimit(defaultEleCountPower);
            }
        }

        if (change) {
            _savePower(userId, smartCodePower);
        }

        return smartCodePower;
    }

    /**
     * 直接存储，不做验证
     * @param userId
     * @param smartCodePower
     */
    void _savePower(@NotNull Long userId, @NotNull SmartCodePower smartCodePower) {
        String key = CacheKeyPrefix.SMART_CODE_POWER_MAP;
        String field = userId.toString();
        cacheService.hSet(key, field, smartCodePower);
    }

    public void savePower(@NotNull Long userId, @NotNull SmartCodePower smartCodePower) {
        // 当前数据
        SmartCodePower power = getPower(userId);
        // 修改当前数据
        if (smartCodePower.getCodeCountLimit() != null) {
            power.setCodeCountLimit(smartCodePower.getCodeCountLimit());
        }
        if (smartCodePower.getEleCountLimit() != null) {
            power.setEleCountLimit(smartCodePower.getEleCountLimit());
        }

        _savePower(userId, power);
    }
}
