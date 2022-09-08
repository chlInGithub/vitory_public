package com.chl.victory.wmall.filter;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.member.query.ShopMemberQueryDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.service.TokenService;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.wmall.model.WmallSessionCache;
import com.chl.victory.wmall.util.ErrorResponseUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.chl.victory.webcommon.manager.RpcManager.memberFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;
import static com.chl.victory.webcommon.manager.RpcManager.miniProgramFacade;

/**
 * 用于小程序wmall，使用cookie和cache 维护 用户分布式session。
 * @author ChenHailong
 * @date 2019/4/15 14:54
 **/
@Component
public class SessionFilter extends OncePerRequestFilter implements Ordered {

    final static String paramNameOfShopId = "shopId";

    final static String paramNameOftId = "tId";

    final static String paramNameOftNick = "tNick";

    final static String paramNameOftImg = "tImg";

    final static String paramNameOftCode = "tCode";

    final static String paramNameOftOpenId = "tOpenId";

    final static String paramNameOftAppId = "appId";

    @Resource
    SessionService sessionService;

    @Resource
    TokenService tokenService;

    public static String getSessionCacheKey(ThirdCommonParam thirdCommonParam) {
        String key = thirdCommonParam.getShopId() + CacheKeyPrefix.SEPARATOR + thirdCommonParam.getThirdId()
                + CacheKeyPrefix.SEPARATOR + StringUtils.reverse(thirdCommonParam.getTOpenId().toUpperCase());
        return key;
    }

    public static ThirdCommonParam getThirdCommonParam(HttpServletRequest request, boolean isTest) {
        ThirdCommonParam thirdCommonParam = new ThirdCommonParam();
        String shopId = SessionFilter.getParamFromRequestOrSession(paramNameOfShopId, request);
        if (NumberUtils.isDigits(shopId)) {
            thirdCommonParam.setShopId(Long.valueOf(shopId));
        }
        thirdCommonParam.setThirdId(SessionFilter.getParamFromRequestOrSession(paramNameOftId, request));
        thirdCommonParam.setThirdNick(SessionFilter.getParamFromRequestOrSession(paramNameOftNick, request));
        thirdCommonParam.setThirdImg(SessionFilter.getParamFromRequestOrSession(paramNameOftImg, request));
        thirdCommonParam.setTCode(SessionFilter.getParamFromRequestOrSession(paramNameOftCode, request));
        thirdCommonParam.setTOpenId(SessionFilter.getParamFromRequestOrSession(paramNameOftOpenId, request));
        thirdCommonParam.setAppId(SessionFilter.getParamFromRequestOrSession(paramNameOftAppId, request));

        return thirdCommonParam;
    }

    private static String getParamFromRequestOrSession(String key, HttpServletRequest request) {
        String val = request.getParameter(key);
        if (StringUtils.isBlank(val)) {
            Cookie cookie = CookieUtil.getCookie(request, key);
            if (null != cookie && StringUtils.isNotBlank(cookie.getValue())) {
                val = cookie.getValue();
            }
        }

        if (StringUtils.isBlank(val)) {
            val = (String) request.getSession().getAttribute(key);
        }
        else {
            request.getSession().setAttribute(key, val);
        }

        return val;
    }

    /**
     * 维护分布式session
     * 首次请求必须携带ThirdCommonParam中参数，后续访问不必携带
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        ThirdCommonParam thirdCommonParam = SessionFilter.getThirdCommonParam(request, false);

        if (thirdCommonParam.getShopId() == null) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }
        if (thirdCommonParam.getAppId() == null) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }
        if (StringUtils.isBlank(thirdCommonParam.getThirdId())) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }
/*        if (StringUtils.isBlank(thirdCommonParam.getTCode())) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }*/

        // 维护session
        try {
            maintainSession(thirdCommonParam, request, response);
        } catch (ServletException e) {
            ErrorResponseUtil.noLogin(request, response, ExceptionUtil.trimExMsg(e));
            return;
        } catch (Throwable e) {
            ErrorResponseUtil.error(request, response, ExceptionUtil.trimExMsg(e));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 维护分布式Session，补全信息
     */
    private void maintainSession(ThirdCommonParam thirdCommonParam, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException, BusServiceException {
        String key = null;
        String domain = null;
        WmallSessionCache sessionCache = null;

        Cookie loginSessionCacheId = CookieUtil.getLoginSessionCacheId(request);
        Cookie loginDomain = CookieUtil.getLoginDomain(request);
        if (loginSessionCacheId != null && loginDomain != null && StringUtils.isNotBlank(loginDomain.getValue())
                && StringUtils.isNotBlank(loginSessionCacheId.getValue())) {
            key = loginSessionCacheId.getValue();
            domain = loginDomain.getValue();
            sessionCache = sessionService.getSession(domain, key, WmallSessionCache.class);
        }

        if (sessionCache != null && sessionCache.isHasFilled()) {
            return;
        }

        if (StringUtils.isBlank(thirdCommonParam.getTCode())) {
            ErrorResponseUtil.noLogin(request, response);
            return;
        }

        sessionCache = fillUpSessionCache(sessionCache, thirdCommonParam);

        if (key == null) {
            // key = sessionCache.getUserId().toString();
            key = getSessionCacheKey(thirdCommonParam);
        }
        if (domain == null) {
            domain = sessionCache.getRootDomain();
        }
        sessionService.setSession(domain, key, sessionCache);

        addCookie(response, sessionCache, domain, key);
    }

    private void addCookie(HttpServletResponse response, WmallSessionCache sessionCache, String domain, String key) {
        CookieUtil.addLoginCookie(response, domain, key, tokenService.genTokenLast(key));
        CookieUtil.addCookie(response, domain, paramNameOfShopId, sessionCache.getShopId().toString());
        CookieUtil.addCookie(response, domain, paramNameOftId, sessionCache.getThirdId());
        /*CookieUtil.addCookie(response, domain, paramNameOftCode, sessionCache.getThirdCode());
        CookieUtil.addCookie(response, domain, paramNameOftOpenId, sessionCache.getThirdOpenId());*/
    }

    public static WmallSessionCache fillUpSessionCache(WmallSessionCache sessionCache, ThirdCommonParam thirdCommonParam)
            throws ServletException, BusServiceException {
        if (sessionCache == null) {
            sessionCache = new WmallSessionCache();
            sessionCache.setShopId(thirdCommonParam.getShopId());
            sessionCache.setThirdId(thirdCommonParam.getThirdId());
            sessionCache.setThirdNick(thirdCommonParam.getThirdNick());
            sessionCache.setThirdImg(thirdCommonParam.getThirdImg());
            sessionCache.setThirdOpenId(thirdCommonParam.getTOpenId());
            sessionCache.setThirdCode(thirdCommonParam.getTCode());
            sessionCache.setAppId(thirdCommonParam.getAppId());
        }

        Long shopId = sessionCache.getShopId();
        ServiceResult<ShopDTO> shopResult = merchantFacade.selectShop(shopId);
        if (!shopResult.getSuccess()) {
            throw new ServletException("未查询到店铺信息");
        }
        ShopDTO shop = shopResult.getData();

        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade.selectShopAppWithValidate(shopId, sessionCache.getAppId());
        if (!shopAppResult.getSuccess()) {
            throw new ServletException("缺少shopApp");
        }
        ShopAppDTO shopAppDO = shopAppResult.getData();

        sessionCache.setShopName(shop.getName());
        sessionCache.setShopImg(shop.getImg());
        sessionCache.setRootDomain(shopAppDO.getDomain());
        sessionCache.setShopMobile(null != shop.getMobile() ? shop.getMobile().toString() : null);
        sessionCache.setShopAddress(null != shop.getAddress() ? shop.getAddress() : null);

        String appid = shopAppDO.getAppId();
        // String secret = shopAppDO.getAppSecret();

        // 获取用户信息
        ServiceResult<WeixinCode2Session> code2SessionResult = miniProgramFacade
                .getCode2Session(shopId, sessionCache.getAppId(), thirdCommonParam.getTCode());
        if (!code2SessionResult.getSuccess()) {
            throw new ServletException(code2SessionResult.getMsg());
        }

        WeixinCode2Session code2SessionFromWeixin = code2SessionResult.getData();

        String openid = code2SessionFromWeixin.getOpenid();
        String sessionKey = code2SessionFromWeixin.getSession_key();

        sessionCache.setThirdCode(thirdCommonParam.getTCode());
        sessionCache.setSessionKey(sessionKey);

        if (org.springframework.util.StringUtils.isEmpty(openid)) {
            throw new ServletException("获取微信信息失败");
        }

        ShopMemberQueryDTO query = new ShopMemberQueryDTO();
        query.setShopId(sessionCache.getShopId());
        query.setThirdId(sessionCache.getThirdId());
        query.setAppId(shopAppDO.getAppId());
        query.setOpenId(openid);
        ServiceResult<List<ShopMemberDTO>> result = memberFacade.selectMem(query);

        if (!result.getSuccess()) {
            throw new ServletException("获取用户信息失败");
        }

        if (CollectionUtils.isEmpty(result.getData())) {
            // 存储店铺和微信用户关系
            ServiceResult saveShopAndWeixinUserResult = miniProgramFacade
                    .saveShopAndWeixinUser(shopId, sessionCache.getThirdId(), appid, openid);

            if (!saveShopAndWeixinUserResult.getSuccess()) {
                throw new ServletException(saveShopAndWeixinUserResult.getMsg());
            }

            result = memberFacade.selectMem(query);
        }

        ShopMemberDTO shopMemberDO = result.getData().get(0);
        sessionCache.setUserId(shopMemberDO.getId());
        sessionCache.setThirdNick(shopMemberDO.getNick());
        sessionCache.setThirdImg(shopMemberDO.getAvatarUrl());
        sessionCache.setMobile(null != shopMemberDO.getMobile() ? shopMemberDO.getMobile().toString() : null);
        sessionCache.setThirdOpenId(openid);
        sessionCache.setRootDomain(shopAppDO.getDomain());
        sessionCache.setHasFilled(true);

        thirdCommonParam.setTOpenId(openid);

        return sessionCache;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 暂时对所有请求都进行session准备工作
        //return false;
        /*String requestURI = request.getRequestURI();*/

        return request.getRequestURI().endsWith("favicon.ico") || request.getRequestURI().endsWith("error") || request
                .getRequestURI().endsWith("css") || request.getRequestURI().endsWith("html")
                || request.getRequestURI().endsWith("js") /*|| request.getRequestURI()
                .endsWith("sessiontimeout.html") || request.getRequestURI().endsWith("noregister.html") || request
                .getRequestURI().endsWith("error.html")*/ || request.getRequestURI().startsWith("/wmall/wx/")
                || request.getRequestURI().startsWith("/wmall/viewSmartCode/")
                || request.getRequestURI().startsWith("/wmall/token/")
                ||  StringUtils.isNotBlank(request.getParameter(CheckSignFilter.PARAM_NAME_V));
    }

    @Data
    public static class ThirdCommonParam {

        /**
         * 第三方用户唯一标识，如微信用户唯一标识
         */
        String thirdId;

        /**
         * 第三方用户名称
         */
        String thirdNick;

        /**
         * 第三方用户头像地址
         */
        String thirdImg;

        String tCode;

        String tOpenId;

        Long shopId;

        String appId;
    }
}
