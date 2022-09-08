package com.chl.victory.web.controller.wm.weixin.thirdplatform;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author ChenHailong
 * @date 2020/5/28 14:02
 **/
@Controller
@RequestMapping("/p/wm/weixin/thirdplatform/redirect/")
public class RedirectController {

    /**
     * 授权后回调
     * @param authCode 授权码
     * @param expires_in 过期时间
     */
    @RequestMapping(path = "afterAuth", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void afterAuth(@RequestParam("auth_code") @NotEmpty String authCode
            ,@RequestParam("expires_in") Integer expires_in, HttpServletResponse response, HttpServletRequest request)
            throws BusServiceException, IOException {

        // 检查登录
        SessionCache sessionCache = SessionUtil.getSessionCache();

        RpcManager.merchantFacade.saveWeixinAuth(sessionCache.getShopId(), sessionCache.getUserId(), authCode, false);

        // 跳转到配置页面
        HttpResponseUtil.sendRedirect(request, response,"/wm/main.html?businessType=config");
    }
}
