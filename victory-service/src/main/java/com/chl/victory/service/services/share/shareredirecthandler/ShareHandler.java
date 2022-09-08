package com.chl.victory.service.services.share.shareredirecthandler;

import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;

/**
 * 访问分享内容的处理器
 * @author ChenHailong
 * @date 2020/8/3 17:53
 **/
public interface ShareHandler {

    /**
     * 返回重定向url
     * @param shortUrlDTO
     * @return
     */
    String redirect(ShortUrlDTO shortUrlDTO);

    void visitShart(ShortUrlDTO shortUrlDTO, Long shopId, Long userId);
}
