package com.chl.victory.wmall.controller.share;

import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.shareFacade;

/**
 * @author ChenHailong
 * @date 2020/10/19 14:27
 **/
@RestController()
@RequestMapping("/wmall/share/")
public class ShareController {
    /**
     * 用于页面header
     * @return
     */
    @GetMapping(path = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result newToken(@RequestParam String scene) {
        if (scene != "0") {
            SessionCache sessionCache = SessionUtil.getSessionCache();
            // 从cache，根据scene找到对应的数据
            ShortUrlDTO shortUrlDTO = shareFacade.get(scene);
            shareFacade.visitShare(sessionCache.getUserId(), sessionCache.getShopId(), scene);
            if (null != shortUrlDTO && shortUrlDTO.getType() != null) {
                return Result.SUCCESS(shortUrlDTO);
            }
        }
        return Result.SUCCESS();
    }

}
