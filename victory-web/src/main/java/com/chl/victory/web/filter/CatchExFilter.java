package com.chl.victory.web.filter;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.web.model.Result;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.chl.victory.common.util.ExceptionUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 捕获异常的filter
 *
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            String result = JSONObject.toJSONString(Result.FAIL(ExceptionUtil.trimExMsg(e), -1));
            HttpResponseUtil.writeJSON(response, result);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return (request.getRequestURI().contains("/r/") && !request.getRequestURI().endsWith(".html"))
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
