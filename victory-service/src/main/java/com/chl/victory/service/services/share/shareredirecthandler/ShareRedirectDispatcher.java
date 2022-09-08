package com.chl.victory.service.services.share.shareredirecthandler;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 访问分享内容处理器的分发器
 * @author ChenHailong
 * @date 2020/12/17 17:36
 **/
@Component
@Validated
public class ShareRedirectDispatcher {

    static Map<Integer, ShareHandler> redirectHandlerMap;

    @PostConstruct
    public void postConstruct() {
        redirectHandlerMap = new HashMap<>();
        redirectHandlerMap.put(ShortUrlDTO.ModelType.share_item.getCode(), new ShareItemHandler());
        redirectHandlerMap.put(ShortUrlDTO.ModelType.share_shop.getCode(), new ShareShopHandler());
        // others
    }

    /**
     * 兼容webview版本
     * @param userId
     * @param shopId
     * @param scene
     * @return
     */
    public String redirect(@NotNull Long userId, @NotNull Long shopId, @NotNull String scene){
        ShortUrlDTO shortUrlDTO;
        ShareHandler shareRedirectHandler;

        // 从cache，根据scene找到对应的数据
        shortUrlDTO = ServiceManager.shortUrlService.get(scene);
        if (null == shortUrlDTO || shortUrlDTO.getType() == null) {
            return null;
        }

        // 根据分享类型处理
        shareRedirectHandler = redirectHandlerMap.get(shortUrlDTO.getType());

        if (null == shareRedirectHandler) {
            shareRedirectHandler = redirectHandlerMap.get(ShortUrlDTO.ModelType.share_shop.getCode());
        }

        String relativeUrl = shareRedirectHandler.redirect(shortUrlDTO);

        if (!userId.equals(shortUrlDTO.getUserId())) {
            shareRedirectHandler.visitShart(shortUrlDTO, shopId, userId);

            ServiceManager.shareService.addGainUser(shopId, shortUrlDTO.getUserId(), scene, userId);
        }

        return relativeUrl;

    }

    public void visitShare(@NotNull Long userId, @NotNull Long shopId, @NotNull String scene) {
        ShortUrlDTO shortUrlDTO;
        ShareHandler shareRedirectHandler;

        // 从cache，根据scene找到对应的数据
        shortUrlDTO = ServiceManager.shortUrlService.get(scene);
        if (null == shortUrlDTO || shortUrlDTO.getType() == null) {
            return;
        }

        if (userId.equals(shortUrlDTO.getUserId())) {
            return;
        }

        // 根据分享类型处理
        shareRedirectHandler = redirectHandlerMap.get(shortUrlDTO.getType());

        if (null == shareRedirectHandler) {
            shareRedirectHandler = redirectHandlerMap.get(ShortUrlDTO.ModelType.share_shop.getCode());
        }

        shareRedirectHandler.visitShart(shortUrlDTO, shopId, userId);

        ServiceManager.shareService.addGainUser(shopId, shortUrlDTO.getUserId(), scene, userId);
    }
}
