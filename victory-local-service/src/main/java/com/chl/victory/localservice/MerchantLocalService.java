package com.chl.victory.localservice;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheService;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ChenHailong
 * @date 2020/9/2 15:19
 **/
public class MerchantLocalService {
    @Resource
    CacheService cacheService;

}
