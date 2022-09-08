package com.chl.victory.serviceimpl.accesslimit;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.accesslimit.AccessLimitFacade;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.accessLimitService;

/**
 * @author ChenHailong
 * @date 2020/9/1 10:18
 **/
@DubboService
public class AccessLimitFacadeImpl implements AccessLimitFacade {

    @Override
    public void doAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix,
            String errorMsg) throws AccessLimitException {
        accessLimitService.doAccessLimit(shopId, typeEnum, subfix, errorMsg);
    }

    @Override
    public void doAccessLimit(@NotNull Long shopId, @NotNull Integer current, @NotNull AccessLimitTypeEnum typeEnum,
            String errorMsg) throws AccessLimitException {
        accessLimitService.doAccessLimit(shopId, current, typeEnum, errorMsg);
    }

    @Override
    public void doAccessLimit(String key, Integer max, Integer period, String errorMsg) throws AccessLimitException {
        accessLimitService.doAccessLimit(key, max, period, errorMsg);
    }

    @Override
    public void checkAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix,
            String errorMsg) throws AccessLimitException {
        accessLimitService.checkAccessLimit(shopId, typeEnum, subfix, errorMsg);
    }

    @Override
    public void incrAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix) {
        accessLimitService.incrAccessLimit(shopId, typeEnum, subfix);
    }
}
