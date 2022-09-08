package com.chl.victory.serviceapi.accesslimit;

import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:05
 **/
public interface AccessLimitFacade {
    void doAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix, String errorMsg)
            throws AccessLimitException;

    void doAccessLimit(@NotNull Long shopId, @NotNull Integer current, @NotNull AccessLimitTypeEnum typeEnum, String errorMsg)
            throws AccessLimitException;

    void doAccessLimit(String key, Integer max, Integer period, String errorMsg) throws AccessLimitException;

    void checkAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix, String errorMsg)
            throws AccessLimitException;

    void incrAccessLimit(@NotNull Long shopId, @NotNull AccessLimitTypeEnum typeEnum, String subfix);
}
