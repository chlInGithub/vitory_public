package com.chl.victory.serviceimpl.test.services.weixin;

import com.chl.victory.serviceimpl.test.BaseTest;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:32
 **/
public class WeixinMiniProgramServiceTest extends BaseTest {
    /*@Resource
    WeixinMiniProgramService weixinService;

    @Test
    public void testGenCode() throws BusServiceException {
        @NotNull Long shopId = 1L;
        @NotEmpty String appId = "wx66e252cb46abe8e4";
        @NotNull ShopAppDO shopAppDO = ServiceManager.merchantService.selectShopAppWithValidate(shopId, appId);
        for (int i = 0; i < 5; i++) {
            weixinService.genCode(shopAppDO, null);
            System.out.println();
        }
    }

    @Test
    public void testGenCode1() throws BusServiceException {
        String appId = "wx66e252cb46abe8e4";
        String secret = "ac7035fbf021c532e6dcf77884b74944";
        ShopAppDO shopAppDO = new ShopAppDO();
        shopAppDO.setAppId(appId);
        shopAppDO.setAppSecret(secret);

        WeixinACodeParam weixinACodeParam = new WeixinACodeParam();
        for (int i = 0; i < 5; i++) {
            try {
                byte[] bytes = weixinService.genCode(shopAppDO, weixinACodeParam);
                File file = new File("D:/workspace/code"+i+".jpg");
                FileUtils.writeByteArrayToFile(file, bytes, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test() throws BusServiceException {
        @NotNull Long shopId = 1L;
        @NotEmpty String appId = "wx66e252cb46abe8e4";
        @NotNull ShopAppDO shopAppDO = ServiceManager.merchantService.selectShopAppWithValidate(shopId, appId);
        @NotNull Long orderId = 3020071415015010005L;
        RefundQueryResult refundQueryResult = weixinService.checkRefunViaWXSDK(shopAppDO, orderId);
        System.out.println(refundQueryResult);
    }
    
    @Test
    public void payUnifiedOrder() {
        *//*WeixinPayUnifiedOrderResult weixinPayUnifiedOrderResult = weixinService.payUnifiedOrder();
        System.out.println(weixinPayUnifiedOrderResult);*//*

        PayUnifiedOrderParam payUnifiedOrderParam = new PayUnifiedOrderParam();
        payUnifiedOrderParam.setAppid("wxd930ea5d5a258f4f");
        payUnifiedOrderParam.setMch_id("10000100");
        payUnifiedOrderParam.setBody("test");
        payUnifiedOrderParam.setNonce_str("ibuaiVcKdpRxkhJA");
        payUnifiedOrderParam.setNotify_url("http://www.weixin.qq.com/wxpay/pay.php");
        payUnifiedOrderParam.setOut_trade_no("20150806125346");
        payUnifiedOrderParam.setSpbill_create_ip("192.168.0.103");
        payUnifiedOrderParam.setTotal_fee(1);
        payUnifiedOrderParam.setTrade_type("JSAPI");

        Map<String, String> params = new HashMap<>();
        params.put("appid", payUnifiedOrderParam.getAppid());
        params.put("mch_id", payUnifiedOrderParam.getMch_id());
        params.put("body", payUnifiedOrderParam.getBody());
        params.put("nonce_str", payUnifiedOrderParam.getNonce_str());
        params.put("notify_url", payUnifiedOrderParam.getNotify_url());
        params.put("out_trade_no", payUnifiedOrderParam.getOut_trade_no());
        params.put("spbill_create_ip", payUnifiedOrderParam.getSpbill_create_ip());
        params.put("total_fee", payUnifiedOrderParam.getTotal_fee().toString());
        params.put("trade_type", payUnifiedOrderParam.getTrade_type());

        String sign = null;
        try {
            sign = WXPayUtil.generateSignature(params, "192006250b4c09247ec02edce69f6a2d", WXPayConstants.SignType.HMACSHA256);
        } catch (Exception e) {
            e.printStackTrace();
        }
        payUnifiedOrderParam.setSign(sign);

*//*        XStream xStream = new XStream();
        xStream.alias("xml", WeixinMiniProgramService.PayUnifiedOrderParam.class);
        String xml = xStream.toXML(payUnifiedOrderParam);
        System.out.println(xml);*//*
        String request = payUnifiedOrderParam.toXml();
        request = request.replaceAll("__", "_");
        System.out.println(request);

        String result = null;

        XStream xStream = new XStream();
        xStream.alias("xml", PayUnifiedOrderResult.class);
        PayUnifiedOrderResult weixinPayUnifiedOrderResult = (PayUnifiedOrderResult) xStream.fromXML(result);
        System.out.println(weixinPayUnifiedOrderResult);
    }

    @Test
    public void getAccessToken() {
        *//*String appId = "wx945e240926afff8a";
        String secret = "0c79a5cf75274f32782203d52f2896c3";
        WeixinAccessToken accessToken = weixinService.getAccessToken(appId, secret, shopAppDO);
        Assert.assertNotNull(accessToken);
        System.out.println(accessToken);*//*
    }
    @Test
    public void getCode2Session() {
        String appId = "wx945e240926afff8a";
        String secret = "0c79a5cf75274f32782203d52f2896c3";
        String code = "0a1tGJqF0VUyVe2jxjpF0ZWoqF0tGJq0";
        WeixinCode2Session code2SessionFromWeixin = weixinService.getCode2SessionFromWeixin(appId, secret, code);
        Assert.assertNotNull(code2SessionFromWeixin);
        System.out.println(code2SessionFromWeixin);
    }

    public static void main(String[] args) {
        String encryptedData = "BTSHeW6xLLuMo82bWGC+giFtZYI5ujjqMNgbB1FnZg+ht4M+w9ULBSV5wfc66pN3jNomWzQ0s+gSbyrxmNjz0dca0OR1btFiDjluAOhYRZy2SeLu9E1HZwCfHq4XgZsb0bwme2mNgIZs9+fJpu5paKb27LIVUO7lD0WN9j65rhumP3HoBVAHJZEEYZVClQIEn4VnibwMndnJZeeOXtpT7g==";
        String accessKey = "dverohthccntfnlR+gEuDQ==";
        String iv = "nMZVknUptJatwPZuHmVnmw==";

        WeixinPhone weixinPhone = WeixinMiniProgramUtil.getWeixinPhone(accessKey, encryptedData, iv);
        System.out.println(weixinPhone);
        weixinPhone = WeixinMiniProgramUtil.getWeixinPhone(accessKey, encryptedData, iv);
        System.out.println(weixinPhone);
    }*/

}
