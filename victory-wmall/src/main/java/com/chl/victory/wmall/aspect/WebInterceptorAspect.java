package com.chl.victory.wmall.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Controller拦截器Aspect
 */
@Aspect
@Component
@Slf4j
public class WebInterceptorAspect {

    private static ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.chl.victory.wmall.controller..*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());

        // ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // if(Objects.nonNull(attributes)) {
        //     HttpServletRequest request = attributes.getRequest();
        //
        //     LOGGER.info("url : {} , full method : {}.{}({})", request.getRequestURL().toString(),
        //             joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
        //             Arrays.toString(joinPoint.getArgs()));
        // }
    }

    @AfterReturning(pointcut = "pointcut()")
    public void doAfterReturning(JoinPoint joinPoint) {
        long time = System.currentTimeMillis() - startTime.get();
        long max = 1000L;
        if (time > max) {
            log.warn("{}.{} , elapsed time : {} ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), time);
        }
        startTime.remove();
    }
}
