package com.chl.victory.wmall.filter;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.wmall.util.ErrorResponseUtil;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 捕获异常的filter
 * @author ChenHailong
 * @date 2019/4/15 14:54
 **/
@Component
public class CatchExFilter extends OncePerRequestFilter implements Ordered {

    /**
     * sessionCacheId token last
     * (暂不加)appname_last_heart_beat_time 最近一次访问时间
     * (暂不加)appname_sso_token 单点登录token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            ErrorResponseUtil.error(request, response, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().endsWith("favicon.ico") || request.getRequestURI().endsWith("html")
                || request.getRequestURI().endsWith("css") || request.getRequestURI().endsWith("js");
    }
}
