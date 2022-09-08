package com.chl.victory.wmall.controller.token;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.ws.RequestWrapper;

import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.wmall.filter.SessionFilter;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ChenHailong
 * @date 2020/10/9 15:54
 **/
@RestController()
@RequestMapping("/wmall/token/")
public class TokenController {
    @Autowired
    HttpServletRequest httpServletRequest;


    @Resource
    SessionService sessionService;

    /**
     * 用于页面header
     * @return
     */
    @PostMapping(path = "newToken", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result newToken(@Validated NewTokenParam param) throws ServletException, BusServiceException {
        // sessionId
        String sessionId = genSessionId(param);
        // token
        String token = genToken(param, sessionId);

        // 用户 店铺
        SessionFilter.ThirdCommonParam thirdCommonParam = new SessionFilter.ThirdCommonParam();
        thirdCommonParam.setAppId(param.getAppId());
        thirdCommonParam.setThirdId(param.getTId());
        thirdCommonParam.setShopId(param.getShopId());
        thirdCommonParam.setTCode(param.getCode());
        WmallSessionCache sessionCache = SessionFilter.fillUpSessionCache(null, thirdCommonParam);

        long invalidTime = System.currentTimeMillis()/1000 + 3600;
        sessionCache.setToken(token);
        sessionCache.setInvalidTime(invalidTime);
        // cache
        sessionService.setSession(param.getAppId(), sessionId, sessionCache);

        TokenVO tokenVO = new TokenVO();
        tokenVO.setSessionId(sessionId);
        tokenVO.setToken(token);
        int i = (int)invalidTime;
        tokenVO.setExpire(i);
        return Result.SUCCESS(tokenVO);
    }

    private String genToken(NewTokenParam param, String sessionId) {
        return Math.abs(param.toString().concat(sessionId).hashCode()) + "";
    }

    private String genSessionId(NewTokenParam param) {
        return Math.abs(param.hashCode()) + "";
    }

    @Data
    public static class TokenVO{

        /**
         *
         */
        String token;

        /**
         * 会话ID
         */
        String sessionId;

        /**
         * token超时绝对时间，单位秒
         */
        Integer expire;
    }

    @Data
    public static class NewTokenParam{
        @NotNull
        Long shopId;

        /**
         * @see com.chl.victory.common.enums.ThirdPlatformEnum
         */
        @NotNull
        String tId;

        /**
         * 应用ID，如小程序appId
         */
        @NotEmpty
        String appId;

        /**
         * 登录code，如微信登录code
         */
        @NotEmpty
        String code;

        /**
         * request域名
         */
        //@NotEmpty
        //String requestDomain;
    }
}
