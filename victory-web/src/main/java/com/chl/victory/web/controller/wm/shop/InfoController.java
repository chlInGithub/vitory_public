package com.chl.victory.web.controller.wm.shop;

import java.util.List;
import javax.validation.constraints.NotNull;

import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p/wm/info/")
public class InfoController {

    @PostMapping(path = "see", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result see(@NotNull Long id) {
        LocalServiceManager.infoLocalService.see(id);
        return Result.SUCCESS();
    }

    /**
     * 店铺基本信息
     * @return
     */
    @GetMapping(path = "get", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result query() {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        List<InfoDTO> infos = LocalServiceManager.infoLocalService.getInfos(sessionCache.getShopId());

        return Result.SUCCESS(infos);
    }
}
