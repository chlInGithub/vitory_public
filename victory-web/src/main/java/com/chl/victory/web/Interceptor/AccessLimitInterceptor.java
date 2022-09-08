package com.chl.victory.web.Interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import com.chl.victory.web.model.Result;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;

/**
 * pre：session维度 访问限流
 * @author ChenHailong
 * @date 2020/4/10 10:29
 **/
@Component
@Slf4j
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();

        try {
            if (requestURI.equals("/p/wm/login") || requestURI.equals("/p/wm/npk")) {
                String key =
                        CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + "login" + CacheKeyPrefix.SEPARATOR + request.getSession()
                                .getId();
                accessLimitFacade.doAccessLimit(key, 4, 10, null);
            }
            else {
                SessionCache sessionCache = SessionUtil.getSessionCache();

                accessLimitFacade
                        .doAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WM_SESSION_SHOP_ALL_INTERFACE,
                                sessionCache.getUserId() + "", null);
                boolean ignore = requestURI.equals("/p/wm/info/get");
                if (!ignore) {
                    accessLimitFacade
                            .doAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WM_SHOP_ALL_INTERFACE, null,
                                    AccessLimitTypeEnum.WM_SHOP_ALL_INTERFACE.getDesc());
                }

            }
        } catch (AccessLimitException e) {
            HttpResponseUtil.writeJSON(response, JSONObject.toJSONString(Result.FAIL(e.getMessage())));
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
