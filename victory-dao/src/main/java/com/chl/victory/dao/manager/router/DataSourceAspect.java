package com.chl.victory.dao.manager.router;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据源代理
 *
 * @author
 */
@Aspect
//@Component
@Order(1)
public class DataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    @Pointcut("execution(* com.chl.victory.dao.manager..*(..))")
    public void dsAspect() {
    }

    @Around("dsAspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Class targetClass = pjp.getTarget().getClass();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        DataSource dataSource = null;
        if (method.isAnnotationPresent(DataSource.class)) {
            dataSource = method.getAnnotation(DataSource.class);
        } else if (targetClass.isAnnotationPresent(DataSource.class)) {
            dataSource = (DataSource) targetClass.getAnnotation(DataSource.class);
        }
        if (dataSource != null) {
            DynamicDataSourceHolder.setDataSource(dataSource.name());
            logger.info("dataSource has been switched to " + dataSource.name());
        } else {
            DynamicDataSourceHolder.setDataSource(DataSource.dsPromotion);
        }
        Object result = pjp.proceed();
        if (method.isAnnotationPresent(DataSource.class) || targetClass.isAnnotationPresent(DataSource.class)) {
            DynamicDataSourceHolder.clearDataSource();
        }
        return result;
    }
}
