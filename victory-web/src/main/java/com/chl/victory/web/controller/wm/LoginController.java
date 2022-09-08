package com.chl.victory.web.controller.wm;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.web.model.Result;
import com.chl.victory.web.service.LoginService;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.service.TokenService;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.webcommon.util.SessionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
@RequestMapping("/p/wm/")
public class LoginController {

    @Resource
    HttpServletResponse httpServletResponse;

    @Resource
    HttpServletRequest httpServletRequest;

    @Resource
    LoginService loginService;

    @Resource
    SessionService sessionService;
    @Resource
    TokenService tokenService;

    /**
     * @param mobile 密文手机号
     * @param pass 密文密码
     * @return
     */
    @PostMapping(path = "login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result login(@RequestParam("n") String mobile, @RequestParam("p") String pass) {

        ServiceResult<MerchantUserDTO> verifyResult = loginService
                .verifyLogin(mobile, pass, httpServletRequest.getSession().getId());
        if (verifyResult.getSuccess()) {
            // 通过mobile查询到user，userID作为cacheId，用于缓存
            String sessionCacheId = verifyResult.getData().getId() + "";
            SessionCache sessionCache = new SessionCache();
            sessionCache.setUserId(verifyResult.getData().getId());
            sessionCache.setMobile(verifyResult.getData().getMobile() + "");
            sessionCache.setModifiedTime(System.currentTimeMillis());

            ServiceResult<List<ShopDTO>> shopResult = RpcManager.merchantFacade
                    .selectShops(verifyResult.getData().getId());
            if (shopResult.getSuccess() && shopResult.getData().size() > 0) {
                sessionCache.setShopName(shopResult.getData().get(0).getName());
                sessionCache.setShopId(shopResult.getData().get(0).getId());
                sessionCache.setInvalidTime(shopResult.getData().get(0).getExpiredTime().getTime());
            }

            String host = httpServletRequest.getHeader("Host");
            sessionService.setSession(host, sessionCacheId, sessionCache);
            CookieUtil.addLoginCookie(httpServletResponse, host, sessionCacheId,
                    tokenService.genTokenLast(sessionCacheId));
            return Result.SUCCESS();
        }

        return Result.FAIL();

    }

    @RequestMapping(path = "logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result logout() {
        // 删除session cookie
        CookieUtil.delLoginCookie(httpServletRequest, httpServletResponse);
        SessionCache sessionCache = SessionUtil.getSessionCache();
        if (null != sessionCache) {
            String host = httpServletRequest.getHeader("Host");
            sessionService.delSession(host, sessionCache.getUserId());
        }
        return Result.SUCCESS();
    }

    /**
     * @return 生成一个密钥对，并返回公钥
     */
    @GetMapping(path = "/npk", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result<String> npk() {
        String pk = loginService.genPublicK(httpServletRequest.getSession().getId());
        if (StringUtils.isNotBlank(pk)) {
            return Result.SUCCESS(pk);
        }

        return Result.FAIL();
    }
}
