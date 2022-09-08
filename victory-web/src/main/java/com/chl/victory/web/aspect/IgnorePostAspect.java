package com.chl.victory.web.aspect;

import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.web.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 体验店不允许写操作
 */
@Aspect
@Component
@Slf4j
public class IgnorePostAspect {

    @Pointcut("@annotation(IgnoreExperience)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        if (RpcManager.merchantFacade.isExperienceShop(sessionCache.getShopId())) {
            return Result.FAIL("体验店只有查询权限。");
        }

        return joinPoint.proceed();
    }
}
