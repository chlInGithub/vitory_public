package com.chl.victory.serviceimpl.test.merchant;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/8/19 17:18
 **/
public class ShopPowerServiceTest extends BaseTest {

    @Test
    public void test(){
        boolean hasValidWeiSales = ServiceManager.shopPowerService.hasValidWeiSales(1L);
        System.out.println();
    }
}
