package com.chl.victory.service.services.share.shareredirecthandler;

import javax.annotation.Resource;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import lombok.NonNull;

/**
 * 店铺分享处理
 * @author ChenHailong
 * @date 2020/8/3 17:57
 **/
public class ShareShopHandler implements ShareHandler {

    @Resource
    CacheService cacheService;

    @Override
    public String redirect(ShortUrlDTO shortUrlDTO) {
        return null;
    }

    @Override
    public void visitShart(ShortUrlDTO shortUrlDTO, Long shopId, Long userId) {
        if (shortUrlDTO != null && shortUrlDTO.getUserId() != null) {
            // 用户店铺分享 对应  引流量
            String key = CacheKeyPrefix.SHARE_PV_SHOP_USER_HASH;
            String field = shortUrlDTO.getShopId() + CacheKeyPrefix.SEPARATOR + shortUrlDTO.getUserId();
            cacheService.hIncrement(key, field, 1);
        }

        // 店铺分享 对应  引流量
        String key = CacheKeyPrefix.SHARE_PV_SHOP_HASH;
        String field = shortUrlDTO.getShopId().toString();
        cacheService.hIncrement(key, field, 1);
    }
}
