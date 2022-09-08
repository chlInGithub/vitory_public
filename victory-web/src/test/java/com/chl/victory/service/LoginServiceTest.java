package com.chl.victory.service;

import com.chl.victory.BaseTest;
import com.chl.victory.core.rsa.RSACoder;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.web.service.LoginService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class LoginServiceTest extends BaseTest {
    @Resource
    LoginService loginService;

    @Test
    public void verifyLogin() {
        String sessionId = "sessionId123";
        String name = "chlName";
        String pass = "abc123";
        String publicK = loginService.genPublicK(sessionId);
        Assert.assertNotNull(publicK);
        ServiceResult serviceResult = loginService.verifyLogin(
                RSACoder.encryptByPublicKey(name, publicK),
                RSACoder.encryptByPublicKey(pass, publicK),
                sessionId);
        Assert.assertTrue(serviceResult.getSuccess());
    }
}