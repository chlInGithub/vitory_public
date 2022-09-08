package com.chl.victory.serviceimpl.test.druid;

import com.alibaba.druid.filter.config.ConfigTools;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/8/20 10:58
 **/
public class ConfigToolsTest {
    @Test
    public void test() throws Exception {
        String password = "xxxxxxxxx";
        String[] arr = ConfigTools.genKeyPair(512);
        String privateKey = arr[0];
        String publicKey = arr[1];
        String afterPassword = ConfigTools.encrypt(arr[0], password);
        System.out.println("privateKey:" + privateKey);
        System.out.println("publicKey:" + publicKey);
        System.out.println("password:" + afterPassword);

    }
}
