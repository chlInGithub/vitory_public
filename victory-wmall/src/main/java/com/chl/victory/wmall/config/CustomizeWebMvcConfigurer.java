package com.chl.victory.wmall.config;

import com.chl.victory.webcommon.config.CommonWebMvcConfigurer;
import com.chl.victory.wmall.Interceptor.AccessLimitInterceptor;
import com.chl.victory.wmall.Interceptor.SessionInterceptor;
import com.chl.victory.wmall.Interceptor.ShareRedirectInterceptor;
import com.chl.victory.wmall.Interceptor.ShopManInterceptor;
import org.springframework.context.annotation.Configuration;
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
        registry.addInterceptor(applicationContext.getBean(SessionInterceptor.class)).addPathPatterns("/**")
                .excludePathPatterns("/**/*.js", "/**/*.css", "/**/error", "/**/*.html", "/wmall/token/**", "/wmall/viewSmartCode/**").order(1);
        // , "/**/error.html", "/**/noregister.html", "/**/sessiontimeout.html"
        registry.addInterceptor(applicationContext.getBean(AccessLimitInterceptor.class)).addPathPatterns("/**")
                .excludePathPatterns("/**/*.js", "/**/*.css", "/**/error", "/**/*.html", "/wmall/token/**", "/wmall/viewSmartCode/**")
                .order(10);
        registry.addInterceptor(applicationContext.getBean(ShopManInterceptor.class))
                .addPathPatterns("/wmall/shopman/**").excludePathPatterns("/wmall/shopman/verify").order(15);
        // , "/**/error.html", "/**/noregister.html", "/**/sessiontimeout.html"
        registry.addInterceptor(applicationContext.getBean(ShareRedirectInterceptor.class))
                .addPathPatterns("/**/shop.html").order(20);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //registry.addResourceHandler("/*.html","/*.css").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/public", "classpath:/static/")
                //.setCachePeriod(86400);
                .setCachePeriod(10);
    }

}
