package com.chl.victory.service.services.share.shareredirecthandler;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import lombok.NonNull;

/**
 * 微导购 则记录关系：当前用户+商品：分享者
 * @author ChenHailong
 * @date 2020/8/3 17:57
 **/
public class ShareItemHandler implements ShareHandler {

    @Override
    public String redirect(ShortUrlDTO shortUrlDTO) {
        return "/itemdetail.html?id=" + shortUrlDTO.getItemId();
    }

    @Override
    public void visitShart(ShortUrlDTO shortUrlDTO, Long shopId, Long userId) {
        if (shortUrlDTO.getShopId() == null || shortUrlDTO.getUserId() == null || shortUrlDTO.getItemId() == null) {
            return;
        }

        ServiceManager.shareService.saveUserItemAndSharer(userId, shortUrlDTO.getItemId(), shortUrlDTO.getUserId());
    }
}
