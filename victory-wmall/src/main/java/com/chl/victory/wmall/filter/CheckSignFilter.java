package com.chl.victory.wmall.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.core.aes.AESCoder;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.wmall.filter.SessionFilter.ThirdCommonParam;
import com.chl.victory.wmall.model.WmallSessionCache;
import com.chl.victory.wmall.util.ErrorResponseUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 用于小程序wmall，验签
 * @author ChenHailong
 * @date 2019/4/15 14:54
 **/
@Component
public class CheckSignFilter extends OncePerRequestFilter implements Ordered {

    public static final String PARAM_NAME_V = "v";

    @Resource
    SessionService sessionService;

    public final static String PARAM_NAME_SESSIONID = "sessionId";
    final static String PARAM_NAME_TOKEN = "token";
    public final static String PARAM_NAME_APPID = "appId";
    final static String PARAM_NAME_SHOPID = "shopId";
    final static String PARAM_NAME_T = "t";
    final static String PARAM_NAME_SIGN = "sign";

    private final String SIGN_FILL_TEMP = "signsignsignsign";

    /**
     * @param request
     * @return 数据错误则null
     */
    private KeyAndIv getKeyIV(HttpServletRequest request, boolean isTokenRequest) {
        KeyAndIv keyAndIv;
        String appId;
        String shopId;

        if (isTokenRequest) {
            // 获取token请求
            appId = request.getParameter(PARAM_NAME_APPID);
            shopId = request.getParameter(PARAM_NAME_SHOPID);
        }else {
            // 业务请求
            String sessionId = request.getParameter(PARAM_NAME_SESSIONID);
            appId = request.getParameter(PARAM_NAME_APPID);
            WmallSessionCache session = sessionService.getSession(appId, sessionId, WmallSessionCache.class);
            appId = session.getAppId();
            shopId = session.getShopId().toString();
        }

        if (StringUtils.isBlank(appId) || StringUtils.isBlank(shopId)) {
            return null;
        }

        appId = appId.length() >= 16 ?
                appId.substring(0, 16) :
                appId + SIGN_FILL_TEMP.substring(0, 16 - appId.length());
        shopId = shopId.length() >= 16 ?
                shopId.substring(0, 16) :
                shopId + SIGN_FILL_TEMP.substring(0, 16 - shopId.length());

        String key = appId.concat(shopId);
        String iv = key.substring(0, 16);

        keyAndIv = new KeyAndIv(key, iv);
        return keyAndIv;
    }

    @Data
    @AllArgsConstructor
    class KeyAndIv{
        String key;
        String iv;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean isTokenRequest = request.getRequestURI().startsWith("/wmall/token/");

        KeyAndIv keyIV = getKeyIV(request, isTokenRequest);
        if (keyIV == null) {
            ErrorResponseUtil.noSign(request, response);
            return;
        }

        boolean checkSignResult;
        if (isTokenRequest) {
            checkSignResult = checkSign4TokenRequest(request, response, keyIV);
        }else {
            checkSignResult = checkSign4BusRequest(request, response, keyIV);
        }

        if (!checkSignResult) {
            ErrorResponseUtil.noSign(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkSign4BusRequest(HttpServletRequest request, HttpServletResponse response, KeyAndIv keyIV)
            throws UnsupportedEncodingException {
        String[] appendKeys = new String[]{PARAM_NAME_APPID, PARAM_NAME_TOKEN, PARAM_NAME_SESSIONID, PARAM_NAME_T, PARAM_NAME_V};
        return checkSign(appendKeys, request, keyIV);
    }

    private boolean checkSign4TokenRequest(HttpServletRequest request, HttpServletResponse response, KeyAndIv keyIV)
            throws UnsupportedEncodingException {
        String[] appendKeys = new String[]{PARAM_NAME_T, PARAM_NAME_V};
        return checkSign(appendKeys, request, keyIV);
    }

    private boolean checkSign(String[] appendKeys, HttpServletRequest request, KeyAndIv keyIV)
            throws UnsupportedEncodingException {
        List<String> appendParams = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet().stream().collect(Collectors.toSet());
        for (String appendKey : appendKeys) {
            String parameter = request.getParameter(appendKey);
            if (StringUtils.isBlank(parameter)) {
                return false;
            }
            appendParams.add(appendKey + "=" + parameter);
            keySet.remove(appendKey);
        }
        String appendData = StringUtils.join(appendParams, "&");

        //String sign = URLDecoder.decode(request.getParameter(PARAM_NAME_SIGN), "utf-8");
        String sign = CheckSignFilter.replace4Spe(request.getParameter(PARAM_NAME_SIGN));
        keySet.remove(PARAM_NAME_SIGN);

        List<String> sortedKeyList = keySet.stream().sorted().collect(Collectors.toList());
        List<String> originalParams = new ArrayList<>();
        for (String key : sortedKeyList) {
            String val = request.getParameter(key);
            originalParams.add(key + "=" + val);
        }
        String originalData = StringUtils.join(originalParams, "&");

        String lastData = StringUtils.isBlank(originalData) ? appendData : originalData + "&" + appendData;

        String signTemp;
        try {
            signTemp = AESCoder.encryptAES(lastData.getBytes(), keyIV.getKey(), keyIV.getIv());
        } catch (Exception e) {
            return false;
        }
        //signTemp = signTemp.replaceAll("[\\&\\=\\+\\$\\,\\#]+", "");
        //signTemp = replace4Spe(signTemp);

        boolean signResult = signTemp.equals(sign);

        if (!signResult) {
            int i = 1;
        }
        return signResult;
    }

    public static String replace4Spe(String original){
        return original.replaceAll("==plus==", "+");
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 5;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 暂时对所有请求都进行session准备工作
        //return false;
        /*String requestURI = request.getRequestURI();*/

        return request.getRequestURI().startsWith("favicon.ico") || request.getRequestURI().endsWith("error") || request
                .getRequestURI().endsWith("css") || request.getRequestURI().endsWith("js") || request.getRequestURI()
                .endsWith("html") || request.getRequestURI().startsWith("/wmall/qrcode/upload") || StringUtils.isBlank(request.getParameter(PARAM_NAME_V));
    }
}
