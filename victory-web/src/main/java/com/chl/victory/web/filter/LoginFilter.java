package com.chl.victory.web.filter;

import com.chl.victory.web.service.LoginService;
import com.chl.victory.webcommon.service.TokenService;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用于管理系统 验证登录Filter
 *
 * @author ChenHailong
 * @date 2019/4/15 14:54
 **/
@Configuration
@ConfigurationProperties("login")
@Data
@Slf4j
public class LoginFilter extends OncePerRequestFilter implements Ordered {
    @Resource
    LoginService loginService;
    @Resource
    TokenService tokenService;

    /**
     * 更新token last的毫秒间隔
     */
    int refreshTokenLastInterval;
    /**
     * sessionCacheId token last
     * (暂不加)appname_last_heart_beat_time 最近一次访问时间
     * (暂不加)appname_sso_token 单点登录token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // System.out.println(request.getRequestURI());
        // 通过cookie判断是否登录过
        if (!CookieUtil.isLogin(request)
                // token last 与cache中数据一致 同一用户并发请求时，可能导致检测未登录
                /*|| !loginService.isLogin(
                        CookieUtil.getLoginToken(request).getValue()+"_"+CookieUtil.getLoginLast(request).getValue(),
                            CookieUtil.getLoginSessionCacheId(request).getValue())*/){
            // System.out.println(request.getRequestURI() + " 检查未登录，跳转登录页面");
            HttpResponseUtil.sendRedirect(request, response, "/wm/login.html");
        }else {
            // 从安全角度考虑，更新token 和 last
            if (System.currentTimeMillis() - Long.valueOf(CookieUtil.getLoginLast(request).getValue()) > refreshTokenLastInterval){
                log.info(request.getRequestURI() + " 更新cookie过期时间");
                CookieUtil.refreshLoginCookie(request,response, CookieUtil.getLoginDomain(request).getValue(), tokenService.genTokenLast(CookieUtil.getLoginSessionCacheId(request).getValue()));
            }
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 10;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        return (request.getRequestURI().startsWith("/wm/r/") && !request.getRequestURI().endsWith(".html"))
                || request.getRequestURI().contains("p/wm/weixin/thirdplatform/notify/")
                || request.getRequestURI().contains("p/wm/weixin/pay/notify/")
                || request.getRequestURI().endsWith("favicon.ico")
                || request.getRequestURI().endsWith("login.html")
                || request.getRequestURI().endsWith("login")
                || request.getRequestURI().endsWith("logout")
                || request.getRequestURI().endsWith("npk")
                /*|| request.getRequestURI().endsWith("css")
                || request.getRequestURI().endsWith("js")*/;
    }
}
