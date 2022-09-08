package com.chl.victory.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

import com.chl.victory.BaseTest;
import com.chl.victory.common.httpclient.HttpClientService;
import lombok.Data;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2019/12/19 17:55
 **/
public class HttpClientServiceTest extends BaseTest {
    @Resource
    HttpClientService httpClientService;

    @Test
    public void get() throws URISyntaxException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type={grant_type}&appid={appid}&secret={secret}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("grant_type", "client_credential");
        vars.put("appid", "wx945e240926afff8a");
        vars.put("secret", "0c79a5cf75274f32782203d52f2896c3");
        String result = httpClientService.get(url, vars, String.class);
        System.out.println(result);
    }
}
