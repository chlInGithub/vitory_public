package com.chl.victory.wmall.Interceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.wmall.filter.CheckSignFilter;
import com.chl.victory.wmall.model.WmallSessionCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * pre：获取session信息 --> 登录者相关信息放到threadlocal中
 * after：清空threadlocal数据
 * @author ChenHailong
 * @date 2019/4/12 10:29
 **/
@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Resource
    SessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        SessionUtil.clear();

        String v = request.getParameter(CheckSignFilter.PARAM_NAME_V);
        WmallSessionCache sessionCache;
        if (StringUtils.isBlank(v)) {
            if (request.getRequestURI().startsWith("/wmall/wx/")){
                return true;
            }
            sessionCache = dealV1(request, response);
        }else {
            sessionCache = dealV2(request, response);
        }

        SessionUtil.setSessionCache(sessionCache);
        return true;
    }

    private WmallSessionCache dealV2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String domainName = request.getParameter(CheckSignFilter.PARAM_NAME_APPID);
        String sessionId = request.getParameter(CheckSignFilter.PARAM_NAME_SESSIONID);
        WmallSessionCache sessionCache = sessionService.getSession(domainName, sessionId, WmallSessionCache.class);
        if (null == sessionCache) {
            throw new Exception("缺少分布式session");
        }
        return sessionCache;
    }

    private WmallSessionCache dealV1(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Cookie sessionCacheIdCookie = CookieUtil.getLoginSessionCacheId(request);
        if (null == sessionCacheIdCookie || StringUtils.isBlank(sessionCacheIdCookie.getValue())) {
            throw new Exception("缺少 sessionKey");
        }
        String sessionCacheKey = sessionCacheIdCookie.getValue();
        Cookie loginDomain = CookieUtil.getLoginDomain(request);
        if (null == loginDomain || StringUtils.isBlank(loginDomain.getValue())) {
            throw new Exception("缺少 domain cookie");
        }
        String domain = loginDomain.getValue();

        WmallSessionCache sessionCache = sessionService.getSession(domain, sessionCacheKey, WmallSessionCache.class);
        if (null == sessionCache) {
            throw new Exception("缺少分布式session");
        }

        sessionService.refresh(domain, sessionCacheKey, sessionCache);

        return sessionCache;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        //SessionUtil.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        SessionUtil.clear();
    }
}
