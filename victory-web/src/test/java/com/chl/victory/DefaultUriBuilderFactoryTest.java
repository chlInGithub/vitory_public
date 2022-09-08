package com.chl.victory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * @author ChenHailong
 * @date 2019/12/19 15:50
 **/
public class DefaultUriBuilderFactoryTest {

    @Test
    public void test(){
        Map<String, String> vars = new HashMap<>();
        vars.put("grant_type", "client_credential");
        vars.put("appid", "1");
        vars.put("secret", "2");
        DefaultUriBuilderFactory templateHandler = new DefaultUriBuilderFactory();
        URI uri = templateHandler.expand("https://api.weixin.qq.com/cgi-bin/token?grant_type={grant_type}&appid={appid}&secret={secret}", vars);
        System.out.println(uri);
    }
}
