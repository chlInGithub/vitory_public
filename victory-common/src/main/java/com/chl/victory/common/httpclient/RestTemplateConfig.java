package com.chl.victory.common.httpclient;

import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author ChenHailong
 * @date 2019/12/19 18:04
 **/
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        restTemplateBuilder = restTemplateBuilder.requestFactory(() -> {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            OkHttp3ClientHttpRequestFactory httpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
            return httpRequestFactory;
        });

        return restTemplateBuilder.build();
    }
}
