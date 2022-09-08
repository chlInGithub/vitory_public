package com.chl.victory.webcommon.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

/**
 * @author ChenHailong
 * @date 2020/4/12 16:24
 **/
public class HttpResponseUtil {

    public static void writeJSON(HttpServletResponse response, String val) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(val);
    }

    /**
     * 避免返回http协议，与nginx代理有关
     * @param request
     * @param response
     * @param relativeUrl
     * @throws IOException
     */
    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String relativeUrl) throws IOException {
        response.sendRedirect("https://" + request.getHeader("Host") + relativeUrl);
    }
}
