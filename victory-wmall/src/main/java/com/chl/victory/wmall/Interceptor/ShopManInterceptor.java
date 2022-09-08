package com.chl.victory.wmall.Interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.WmallSessionCache;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * pre：针对店铺管理小程序，验证是否做过店铺人员校验
 * after：清空threadlocal数据
 * @author ChenHailong
 * @date 2019/4/12 10:29
 **/
@Component
public class ShopManInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        deal(request, response);
        return true;
    }

    private void deal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        if (!sessionCache.isShopMan() || sessionCache.getShopId4ShopMan() == null
                || sessionCache.getUserId4ShopMan() == null) {
            throw new Exception("notShopMan");
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
