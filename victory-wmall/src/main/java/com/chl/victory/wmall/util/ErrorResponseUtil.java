package com.chl.victory.wmall.util;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import com.chl.victory.wmall.model.FailEnum;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import org.apache.commons.lang3.StringUtils;

import static com.chl.victory.webcommon.util.HttpResponseUtil.sendRedirect;

/**
 * @author ChenHailong
 * @date 2020/4/12 16:24
 **/
public class ErrorResponseUtil {

    public static void noSign(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*if (request.getRequestURI().endsWith("html")) {
            sendRedirect(request, response,"/wmall/sessiontimeout.html");
            return;
        }*/

        String result = JSONObject.toJSONString(Result.FAIL(FailEnum.NOT_SIGN.getMsg(), FailEnum.NOT_SIGN.getType()));
        HttpResponseUtil.writeJSON(response, result);
    }
    public static void noLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().endsWith("html")) {
            sendRedirect(request, response,"/wmall/sessiontimeout.html");
            return;
        }

        String result = JSONObject.toJSONString(Result.FAIL(FailEnum.NOT_LOGIN.getMsg(), FailEnum.NOT_LOGIN.getType()));
        HttpResponseUtil.writeJSON(response, result);
    }

    public static void noLogin(HttpServletRequest request, HttpServletResponse response, String msg) throws IOException {
        if (request.getRequestURI().endsWith("html")) {
            if (StringUtils.isNotBlank(msg)) {
                msg = URLEncoder.encode(msg, "UTF-8");
            }
            sendRedirect(request, response,"/wmall/sessiontimeout.html?error=" + msg);
            return;
        }

        String result = JSONObject.toJSONString(Result.FAIL(FailEnum.NOT_LOGIN.getMsg(), FailEnum.NOT_LOGIN.getType()));
        HttpResponseUtil.writeJSON(response, result);
    }

    public static void noRegister(HttpServletRequest request, HttpServletResponse response,
            WmallSessionCache sessionCache) throws IOException {
        if (request.getRequestURI().endsWith("html")) {
            sendRedirect(request, response,
                    "/wmall/noregister.html?shopId=" + sessionCache.getShopId() + "&shopName=" + sessionCache
                            .getShopName() + "&shopImg=" + sessionCache.getShopImg());
            return;
        }

        String result = JSONObject
                .toJSONString(Result.FAIL(FailEnum.NOT_REGISTER.getMsg(), FailEnum.NOT_REGISTER.getType()));
        HttpResponseUtil.writeJSON(response, result);
    }

    public static void error(HttpServletRequest request, HttpServletResponse response, String msg) throws IOException {
        if (request.getRequestURI().endsWith("checkPayed") || request.getRequestURI().endsWith("html")) {
            String errorMsg = "出现意外喽";
            if (StringUtils.isNotBlank(msg)) {
                errorMsg = URLEncoder.encode(msg, "UTF-8");
            }
            sendRedirect(request, response, "/wmall/error.html?error=" + errorMsg);
            return;
        }

        String result = JSONObject.toJSONString(Result.FAIL(msg, -1));
        HttpResponseUtil.writeJSON(response, result);
    }
}
