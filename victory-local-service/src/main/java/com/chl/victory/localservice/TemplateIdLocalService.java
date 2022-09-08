package com.chl.victory.localservice;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 代码发布使用的模板ID
 * @author ChenHailong
 * @date 2020/11/13 9:32
 **/
@Component
public class TemplateIdLocalService {
    @Resource
    CacheService cacheService;

    public String getTemplateId(@NotNull String appId){
        String key = CacheKeyPrefix.WEIXIN_CODE_TEMPLATEID_APPID_MAP;
        String templateId = cacheService.hGet(key, appId, String.class);
        if (StringUtils.isBlank(templateId)) {
            key = CacheKeyPrefix.WEIXIN_CODE_TEMPLATEID;
            templateId = cacheService.get(key);
        }
        return templateId;
    }
}
