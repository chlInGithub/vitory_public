package com.chl.victory.serviceimpl.test.services.weixin;

import com.chl.victory.serviceimpl.test.BaseTest;

/**
 * @author ChenHailong
 * @date 2020/2/19 12:08
 **/
public class WeixinPayTest extends BaseTest {
    /*@Test
    public void test1() throws Exception {
        Long shopId = 1L;
        ShopDO shopDO = ServiceManager.merchantService.selectShop(shopId);
        @NotEmpty String appId = "wx66e252cb46abe8e4";
        @NotNull ShopAppDO shopAppDO = ServiceManager.merchantService.selectShopAppWithValidate(shopId, appId);
        @NotNull OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1234567L);
        orderDTO.setRealFee(new BigDecimal("0.01"));
        String openId = "oFRFO5buOEdQqLGOBBX6JvjinzdU";
        for (int i = 0; i < 5; i++) {
            *//*try {
                ServiceManager.weixinMiniProgramService.payUnifiedOrderViaWXSDK(shopDO, shopAppDO, orderDTO, openId);
            } catch (BusServiceException e) {
                e.printStackTrace();
            }*//*
     *//*try {
                ServiceManager.weixinMiniProgramService.payUnifiedOrder(shopDO, shopAppDO, orderDTO);
            } catch (BusServiceException e) {
                e.printStackTrace();
            }*//*
            try {
                ServiceManager.weixinMiniProgramService.checkPayedViaWXSDK(shopDO, shopAppDO, orderDTO);
            } catch (BusServiceException e) {
                e.printStackTrace();
            }
        }

    }*/

}
