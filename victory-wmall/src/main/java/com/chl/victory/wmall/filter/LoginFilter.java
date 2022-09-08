package com.chl.victory.wmall.filter;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.wmall.filter.SessionFilter.ThirdCommonParam;
import com.chl.victory.wmall.model.WmallSessionCache;
import com.chl.victory.wmall.util.ErrorResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 用于小程序wmall，是否存在session code，验证已登录，验证已注册,未注册则返回注册，由view决定跳转
 * @author ChenHailong
 * @date 2019/4/15 14:54
 **/
@Component
public class LoginFilter extends OncePerRequestFilter implements Ordered {

    @Resource
    SessionService sessionService;

    /**
     * 维护分布式session
     * 首次请求必须携带ThirdCommonParam中参数，后续访问不必携带
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // System.out.println(request.getRequestURI());

        // 获取需要传递的通用参数

        String v = request.getParameter(CheckSignFilter.PARAM_NAME_V);
        if (StringUtils.isBlank(v)) {
            dealV1(request, response, filterChain);
        }else {
            dealV2(request, response, filterChain);
        }
    }

    /**
     * 版本2： token
     */
    private void dealV2(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String token = request.getParameter(CheckSignFilter.PARAM_NAME_TOKEN);
        String sessionId = request.getParameter(CheckSignFilter.PARAM_NAME_SESSIONID);
        String t = request.getParameter(CheckSignFilter.PARAM_NAME_T);
        if (StringUtils.isBlank(token) || StringUtils.isBlank(sessionId) || !NumberUtils.isDigits(t)) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }

        String domainName = request.getParameter(CheckSignFilter.PARAM_NAME_APPID);

        WmallSessionCache sessionCache = sessionService.getSession(domainName, sessionId, WmallSessionCache.class);

        // session cache , check token
        if (sessionCache == null || StringUtils.isBlank(sessionCache.getToken()) || !token.equals(sessionCache.getToken())
                || sessionCache.getInvalidTime() < NumberUtils.toLong(t, Long.MAX_VALUE)) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }

        if (sessionCache.getUserId() == null) {
            ErrorResponseUtil.noRegister(request, response, sessionCache);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void dealV1(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException  {
        ThirdCommonParam thirdCommonParam = SessionFilter.getThirdCommonParam(request, false);
        if (thirdCommonParam.getShopId() == null) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }

        Cookie loginDomain = CookieUtil.getLoginDomain(request);
        Cookie loginSessionCacheId = CookieUtil.getLoginSessionCacheId(request);
        // String key = SessionFilter.getSessionCacheKey(thirdCommonParam);
        WmallSessionCache sessionCache = null;
        if (loginDomain != null && loginSessionCacheId != null && StringUtils.isNotBlank(loginDomain.getValue()) && StringUtils.isNotBlank(loginSessionCacheId.getValue())){
            sessionCache = sessionService.getSession(loginDomain.getValue(), loginSessionCacheId.getValue(), WmallSessionCache.class);
        }

        if (sessionCache == null) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }

        if (sessionCache.getUserId() == null) {
            ErrorResponseUtil.noRegister(request, response, sessionCache);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 2;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 暂时对所有请求都进行session准备工作
        //return false;
        /*String requestURI = request.getRequestURI();*/

        return request.getRequestURI().startsWith("favicon.ico") || request.getRequestURI().endsWith("error") || request
                .getRequestURI().endsWith("css") || request.getRequestURI().endsWith("js")
                || request.getRequestURI().endsWith("html")/*|| request.getRequestURI()
                .endsWith("sessiontimeout.html") || request.getRequestURI().endsWith("noregister.html")
                || request.getRequestURI().endsWith("error.html")*/ || request.getRequestURI().startsWith("/wmall/token/")
                || request.getRequestURI().startsWith("/wmall/viewSmartCode/")
                || (StringUtils.isBlank(request.getParameter(CheckSignFilter.PARAM_NAME_V)) && !(request.getRequestURI().startsWith("/wmall/cart/")
                || request.getRequestURI().startsWith("/wmall/settle/") || request.getRequestURI()
                .startsWith("/wmall/order/") || request.getRequestURI().startsWith("/wmall/deliver/") || request
                .getRequestURI().startsWith("/wmall/user/")));
    }
}
