package com.chl.victory.web.config;

import java.util.concurrent.TimeUnit;

import com.chl.victory.web.Interceptor.AccessLimitInterceptor;
import com.chl.victory.web.Interceptor.SessionInterceptor;
import com.chl.victory.webcommon.config.CommonWebMvcConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * 个性化mvcConfigurer
 * @author ChenHailong
 * @date 2019/3/4 15:16
 **/
@Configuration
public class CustomizeWebMvcConfigurer extends CommonWebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(applicationContext.getBean(SessionInterceptor.class)).addPathPatterns("/p/**")
                .excludePathPatterns("/p/wm/login", "/p/wm/npk", "/p/wm/weixin/thirdplatform/notify/**",
                        "/p/wm/weixin/pay/notify/**").order(1);
        // 暂时不验证 form 表单 token
        // registry.addInterceptor(applicationContext.getBean(FormTokenCheckInterceptor.class)).addPathPatterns("/p/**").order(4);
        registry.addInterceptor(applicationContext.getBean(AccessLimitInterceptor.class)).addPathPatterns("/p/**")
                .excludePathPatterns("/p/wm/weixin/thirdplatform/notify/**", "/p/wm/weixin/pay/notify/**").order(5);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //registry.addResourceHandler("/**/*.html","/**/*.css").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/public", "classpath:/static/")
                .setCachePeriod(0)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().noTransform());
    }

}
