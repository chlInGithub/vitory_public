package com.chl.victory.webcommon.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 处理与login相关的cookie
 * @author ChenHailong
 * @date 2019/4/16 10:05
 **/
public class CookieUtil {

    // s
    private final static int MAX_AGE = 3600;

    static final int maxLastInterval = MAX_AGE * 1000;

    private final static String ROOT_PATH = "/";

    private final static String sessionCacheIdKey = "wmall_session_id";

    private final static String tokenKey = "wmall_token";

    private final static String lastHeartKey = "wmall_last";
    private final static String domainKey = "wmall_domain";

    /**
     * 通过name匹配cookie
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static Cookie getLoginSessionCacheId(HttpServletRequest request) {
        return getCookie(request, sessionCacheIdKey);
    }
    public static Cookie getLoginDomain(HttpServletRequest request) {
        return getCookie(request, domainKey);
    }

    public static Cookie getLoginToken(HttpServletRequest request) {
        return getCookie(request, tokenKey);
    }

    public static Cookie getLoginLast(HttpServletRequest request) {
        return getCookie(request, lastHeartKey);
    }

    /**
     * 刷新存储登录信息的cookie
     */
    public static void refreshLoginCookie(HttpServletRequest request, HttpServletResponse response, @NotNull String rootDomain, @NotNull String tokenLast) {
        Cookie userCookie = getCookie(request, sessionCacheIdKey);
        if (userCookie != null) {
            userCookie.setDomain(rootDomain);
            //userCookie.setMaxAge(MAX_AGE);
            userCookie.setPath(ROOT_PATH);
            userCookie.setHttpOnly(true);
            response.addCookie(userCookie);
        }
        String[] tokenAndLast = tokenLast.split("_");
        Cookie tokenCookie = getCookie(request, tokenKey);
        if (tokenCookie != null) {
            tokenCookie.setDomain(rootDomain);
            //tokenCookie.setMaxAge(MAX_AGE);
            tokenCookie.setPath(ROOT_PATH);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setValue(tokenAndLast[ 0 ]);
            response.addCookie(tokenCookie);
        }
        Cookie lastCookie = getCookie(request, lastHeartKey);
        if (lastCookie != null) {
            lastCookie.setDomain(rootDomain);
            //lastCookie.setMaxAge(MAX_AGE);
            lastCookie.setPath(ROOT_PATH);
            lastCookie.setHttpOnly(true);
            lastCookie.setValue(tokenAndLast[ 1 ]);
            response.addCookie(lastCookie);
        }
    }

    public static void delLoginCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie userCookie = getCookie(request, sessionCacheIdKey);
        if (null != userCookie) {
            userCookie.setDomain(request.getHeader("Host"));
            userCookie.setMaxAge(1);
            userCookie.setValue("0");
            userCookie.setPath(ROOT_PATH);
            response.addCookie(userCookie);
        }
    }

    public static boolean isLogin(HttpServletRequest request) {
        Cookie user = getLoginSessionCacheId(request);
        Cookie token = getLoginToken(request);
        Cookie last = getLoginLast(request);
        Cookie loginDomain = getLoginDomain(request);
        boolean commonVerify = null != user && StringUtils.isNotBlank(user.getValue()) && null != token && StringUtils
                .isNotBlank(token.getValue()) && null != last && StringUtils.isNotBlank(last.getValue()) && NumberUtils
                .isDigits(last.getValue()) && loginDomain != null && StringUtils.isNotBlank(loginDomain.getValue());
        if (!commonVerify) {
            return false;
        }

        boolean checkLastBeforeNow = System.currentTimeMillis() > NumberUtils.toLong(last.getValue());
        Long interval = System.currentTimeMillis() - NumberUtils.toLong(last.getValue());
        String tempToken = (last.getValue() + "baseLast").hashCode() + "";
        return commonVerify && checkLastBeforeNow
                && interval < maxLastInterval && tempToken.equals(token.getValue());
    }

    public static void addLoginCookie(HttpServletResponse response, String rootDomain, String user_cookie, String tokenLast) {
        addCookie(response, rootDomain, sessionCacheIdKey, user_cookie);
        addCookie(response, rootDomain, domainKey, rootDomain);

        String[] tokenAndLast = tokenLast.split("_");
        addCookie(response, rootDomain, tokenKey, tokenAndLast[ 0 ]);
        addCookie(response, rootDomain, lastHeartKey, tokenAndLast[ 1 ]);
    }

    public static void addCookie(HttpServletResponse response, String rootDomain, String key, String val) {
        Cookie cookie = new Cookie(key, val);
        cookie.setDomain(rootDomain);
        //userCookie.setMaxAge(MAX_AGE);
        cookie.setPath(ROOT_PATH);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
