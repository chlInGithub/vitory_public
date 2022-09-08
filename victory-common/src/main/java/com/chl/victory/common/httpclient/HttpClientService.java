package com.chl.victory.common.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ChenHailong
 * @date 2019/12/19 17:54
 **/
@Component
@Slf4j
public class HttpClientService {
    @Resource
    RestTemplate restTemplate;

    public <T> T get(String url, Map<String, Object> vars, Class<T> responseDataClazz){
        if (log.isInfoEnabled()) {
            log.info("request {}|{}", url, JSONObject.toJSONString(vars));
        }

        ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, responseDataClazz, vars);
        T body = responseEntity.getBody();

        if (log.isInfoEnabled()) {
            log.info("result {}|{}|{}", url, JSONObject.toJSONString(vars), JSONObject.toJSONString(body));
        }

        return body;
    }

    public <T> T post(String url, String request, Class<T> responseDataClazz) {
        if (log.isInfoEnabled()) {
            log.info("request {}|{}", url, request);
        }

        ResponseEntity<T> postForEntity = restTemplate.postForEntity(url, request, responseDataClazz);
        T body = postForEntity.getBody();

        if (log.isInfoEnabled()) {
            log.info("result {}|{}|{}", url, request, JSONObject.toJSONString(body));
        }

        return body;
    }
    public <T> T post(String url, String request, Class<T> responseDataClazz, Map<String, ?> uriVariables) {
        if (log.isInfoEnabled()) {
            log.info("request {}|{}|{}", url, request, JSONObject.toJSONString(uriVariables));
        }

        ResponseEntity<T> postForEntity = restTemplate.postForEntity(url, request, responseDataClazz, uriVariables);
        T body = postForEntity.getBody();

        if (log.isInfoEnabled()) {
            log.info("result {}|{}|{}|{}", url, request, JSONObject.toJSONString(uriVariables), JSONObject.toJSONString(body));
        }

        return body;
    }
}
