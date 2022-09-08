package com.chl.victory.webcommon.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ChenHailong
 * @date 2020/6/9 18:01
 **/
public class FormTokenUtil {

    /**
     * 生成form token
     * @param request
     * @return
     */
    public static String gen(HttpServletRequest request) {
        String host = request.getHeader("Host");
        String referer = request.getHeader("Referer");
        if (StringUtils.isBlank(host) || StringUtils.isBlank(referer)) {
            throw new FormTokenException("生成表单token失败");
        }

        String token = (host + referer + System.currentTimeMillis()).hashCode() + "";
        return token;
    }

    /**
     * 从request中获取form token
     * @param request
     * @return
     */
    public static String get(HttpServletRequest request) {
        String formToken = request.getHeader("ft");
        if (StringUtils.isBlank(formToken)) {
            throw new FormTokenException("缺失表单token");
        }
        return formToken;
    }

    public static class FormTokenException extends RuntimeException{
        public FormTokenException(String message) {
            super(message);
        }
    }
}
