package com.chl.victory.web.Interceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.CookieUtil;
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
        String sessionCacheId;

        // sessionId -> 从cache获取usercontext信息，若无则session失效，重定向login
        sessionCacheId = CookieUtil.getLoginSessionCacheId(request).getValue();

        Cookie loginDomain = CookieUtil.getLoginDomain(request);
        SessionCache sessionCache = sessionService
                .getSession(loginDomain.getValue(), sessionCacheId, SessionCache.class);
        if (null == sessionCache) {
            response.getWriter().write("noLogin");
            return false;
        }

        sessionService.refresh(loginDomain.getValue(), sessionCacheId, sessionCache);

        SessionUtil.setSessionCache(sessionCache);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        SessionUtil.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
