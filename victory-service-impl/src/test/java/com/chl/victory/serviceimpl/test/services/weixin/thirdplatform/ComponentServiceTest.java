package com.chl.victory.serviceimpl.test.services.weixin.thirdplatform;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/6/15 9:37
 **/
public class ComponentServiceTest extends BaseTest {

    @Test
    public void testQueryAuth() {
        String code = "queryauthcode@@@VTKj0k3eGB826nDIce5plxbMF_HaUK1Hzg1uDbZ6uIWp0eZ_38ysX_QHPB2Jc_yz4MYqnQLMYLimJ9uxB1m6pA";
        ServiceManager.componentService.queryAuth(code);
    }
}
