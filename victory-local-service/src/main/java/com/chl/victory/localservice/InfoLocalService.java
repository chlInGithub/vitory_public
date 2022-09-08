package com.chl.victory.localservice;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.model.InfoDTO;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/9/1 17:08
 **/
@Component
public class InfoLocalService {
    @Resource
    CacheService cacheService;

    public List<InfoDTO> getInfos(@NotNull Long shopId) {
        String key = CacheKeyPrefix.INFO_LIST_OF_SHOP + shopId;
        List<InfoDTO> cache = cacheService.lRange(key, InfoDTO.class);
        cache.stream().forEach(item -> {
            item.setSee(isSee(item.getId()));
        });
        return cache;
    }

    public void see(@NotNull Long id) {
        String key = CacheKeyPrefix.INFO_SEE_OF_SHOP;
        Long offset = id;
        boolean val = true;
        cacheService.setBit(key, offset, val);
    }

    Integer isSee(@NotNull Long id) {
        String key = CacheKeyPrefix.INFO_SEE_OF_SHOP;
        Long offset = id;
        return cacheService.getBit(key, offset) ? 1 : 0;
    }

    /**
     * 添加消息
     * @param info
     * @param shopId
     */
    public void addInfo(@NotNull InfoDTO info, @NotNull Long shopId) {
        if (info.getId() == null) {
            info.setId(genInfoId());
        }

        String key = CacheKeyPrefix.INFO_LIST_OF_SHOP + shopId;
        cacheService.lPushAndTrim(key, info, 20, null);
    }

    /**
     * 生成消息ID
     * @return
     */
    public Long genInfoId() {
        String key = CacheKeyPrefix.INFO_ID;
        return cacheService.incr(key);
    }
}
