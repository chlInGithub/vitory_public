package com.chl.victory.wmall.Interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import com.chl.victory.webcommon.util.SessionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 为了兼容第一版的webview，才创建该拦截器。对第二版即原生组件小程序无作用。
 * @author ChenHailong
 * @date 2019/4/12 10:29
 **/
@Component
public class ShareRedirectInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // scene参数?
        String scene = request.getParameter("scene");
        if (StringUtils.isBlank(scene) || scene.equals("0")) {
            return true;
        }

        Long userId = SessionUtil.getSessionCache().getUserId();
        Long shopId = SessionUtil.getSessionCache().getShopId();
        ServiceResult<String> shareRedirectResult = RpcManager.shareFacade.shareRedirect(userId, shopId, scene);
        String relativeUrl = shareRedirectResult.getData();
        if (StringUtils.isBlank(relativeUrl)) {
            return true;
        }

        HttpResponseUtil.sendRedirect(request, response, relativeUrl);
        return false;
    }

}
