package com.chl.victory.wmall.Interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.filter.CheckSignFilter;
import com.chl.victory.wmall.model.WmallSessionCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

        if (requestURI.contains("/wx/")) {
            String v = request.getParameter(CheckSignFilter.PARAM_NAME_V);
            String sessionId;
            if (StringUtils.isBlank(v)) {
                sessionId = request.getSession().getId();
            }else {
                sessionId = SessionUtil.getSessionCache().getKey();
            }
            String key = CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + "wx" + CacheKeyPrefix.SEPARATOR + sessionId;
            accessLimitFacade.doAccessLimit(key, 4, 10, null);
        }
        else {
            SessionCache sessionCache = SessionUtil.getSessionCache();

            accessLimitFacade
                    .doAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_SESSION_SHOP_ALL_INTERFACE,
                            sessionCache.getUserId() + "", null);
            accessLimitFacade
                    .doAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_SHOP_ALL_INTERFACE, null,
                            AccessLimitTypeEnum.WMALL_SHOP_ALL_INTERFACE.getDesc());

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
