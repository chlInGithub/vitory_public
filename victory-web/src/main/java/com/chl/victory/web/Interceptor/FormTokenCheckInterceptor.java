package com.chl.victory.web.Interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.web.service.LoginService;
import com.chl.victory.webcommon.service.TokenService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.util.FormTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * pre: 检查form token
 * @author ChenHailong
 * @date 2020/4/10 10:29
 **/
@Component
@Slf4j
public class FormTokenCheckInterceptor implements HandlerInterceptor {

    @Resource
    LoginService loginService;
    @Resource
    TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 仅关注post
        if (!HttpMethod.POST.equals(HttpMethod.resolve(request.getMethod()))) {
            return true;
        }

        SessionCache sessionCache = SessionUtil.getSessionCache();
        String shopIdOrSessionId;
        if (null == sessionCache) {
            shopIdOrSessionId = request.getSession().getId();
        }
        else {
            shopIdOrSessionId = sessionCache.getShopId().toString();
        }
        String formToken = FormTokenUtil.get(request);
        tokenService.checkFormToken(formToken, shopIdOrSessionId);

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
