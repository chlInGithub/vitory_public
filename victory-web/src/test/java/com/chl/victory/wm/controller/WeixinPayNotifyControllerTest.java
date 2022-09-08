package com.chl.victory.wm.controller;

import com.chl.victory.BaseTest;
import com.chl.victory.web.controller.wm.weixin.pay.WeixinPayNotifyController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ChenHailong
 * @date 2020/7/14 15:26
 **/
public class WeixinPayNotifyControllerTest extends BaseTest {
    @Autowired
    WeixinPayNotifyController weixinPayNotifyController;
    
    @Test
    public void testPay(){
        // String refundNotify = "<xml><return_code>SUCCESS</return_code><appid><![CDATA[wx66e252cb46abe8e4]]></appid><mch_id><![CDATA[1530932181]]></mch_id><nonce_str><![CDATA[7abac4f5159b96bf7a7dcc2d13248882]]></nonce_str><req_info><![CDATA[5JMoSpmBAnPDV9eK7KhpTHm0mVxfWmttpd0eoO53DdVDN3VTLoRFXFIxK6DxXb9sTIQHynf5psgMokYCJemRPMdkZnjZfuQXKe5PEoyLmafhVA08eDGFICo6wxUe7Pv5vIJRuqnEPvsutZKRVg952COAlS03F79rXp72VJmv1jVT+fyTXvuADVvoOuB2Do2ENqnx3KYJUoxqe7nq+8P3OnSZUncccmBNBAl1vCeyt4l8BNVsw33d13bg6Wdlocp+/2YavVJXphrdVTPRBmyF3oAP6Ol+y7Jb4FrPuX/74JETKez49by5MdFb4kTUqRyaqQXlue/OCtSBzzay/KDr88vtg8eUlS+VfTS9Fp0Iko0Fs25EnxU6anm0iCX1huTZxhtXwXsf0mgo+WgLeOwS3OqdNO8u2IOcp8BWrMnAkDdTk66/1jI+5NzAz+M1idKcPFZIKoW/s/s8F/dg7xMcfHyRM4+fjGev5Y4n/cFE+rEFyT19eZFUxv8QsyrdyIM8gNRuBGBFg0PO237+PGysQYmWYWX6fETa3iKSR5h1maEU+QOW2AJg261YGHFpiuyHl2bzFrVG4aLctcFUbZEhkiEl3C8c3uX1F5firTipsB0an6cS2ll/y1wGiB4hioItToGkhq1YWfN5+IW6n7UjEPOGcp7h6ScYcIXFbKhHmWOPnbWHtLZ8NC6gmDBPvXL6NFwgrzOaLr6BEaHVXpu/LlS54auH5SW7s57ql8481aH+Cz2b3F1Y72VQouuL90G7LBzToDrfoC3roOEJzwhNqEihYg78gvm/sQHpkfTl1+mMk4yoPP9Z62vAdx8j3gp8uDCliuKiMkViHEXhs/YOhqhIzBrJ2n++jeUmh4B1wEnTIPzSbfP0XXJ2vA6AWLynzC5+oZkYgK1qPOzKXa9MqxKgx1ixKNvLnjhPFqU8pMuIEWegp7QiMCp5g5YxA2y4SRam4tV8AeRTfl7O6Z/NfBvskNq4nj8si8xPENIOLovAaVW/+4CcYdmvPrB6FAZu2CMsT0nbd5vNgfYdD3IJPUtkFn9GtYXX7/k6OOysXiw=]]></req_info></xml>";

        String payNotify = "<xml><appid><![CDATA[wx66e252cb46abe8e4]]></appid>\n"
                + "<bank_type><![CDATA[OTHERS]]></bank_type>\n" + "<cash_fee><![CDATA[1]]></cash_fee>\n"
                + "<fee_type><![CDATA[CNY]]></fee_type>\n" + "<is_subscribe><![CDATA[N]]></is_subscribe>\n"
                + "<mch_id><![CDATA[1530932181]]></mch_id>\n"
                + "<nonce_str><![CDATA[kdMU2MLc5bEi8iHslkSONVrxpgTjdEeK]]></nonce_str>\n"
                + "<openid><![CDATA[oFRFO5buOEdQqLGOBBX6JvjinzdU]]></openid>\n"
                + "<out_trade_no><![CDATA[3020071415015010005]]></out_trade_no>\n"
                + "<result_code><![CDATA[SUCCESS]]></result_code>\n"
                + "<return_code><![CDATA[SUCCESS]]></return_code>\n"
                + "<sign><![CDATA[3F96BE2A95FDA88A129AD548DE9D402D6CD0E223570E156CB925C8F8D06380EE]]></sign>\n"
                + "<time_end><![CDATA[20200714150155]]></time_end>\n" + "<total_fee>1</total_fee>\n"
                + "<trade_type><![CDATA[JSAPI]]></trade_type>\n"
                + "<transaction_id><![CDATA[4200000593202007147088861876]]></transaction_id>\n" + "</xml>";
        for (int i=0; i < 5; i++) {
            weixinPayNotifyController.pay(payNotify);
        }
    }
    @Test
    public void testRefund(){
        String refundNotify = "<xml><return_code>SUCCESS</return_code><appid><![CDATA[wx66e252cb46abe8e4]]></appid><mch_id><![CDATA[1530932181]]></mch_id><nonce_str><![CDATA[1486e9d6397a94541cd96d6709bab7cf]]></nonce_str><req_info><![CDATA[5JMoSpmBAnPDV9eK7KhpTHm0mVxfWmttpd0eoO53DdW39MpmW+F+jqBmM90+2LtQTIQHynf5psgMokYCJemRPMdkZnjZfuQXKe5PEoyLmafhVA08eDGFICo6wxUe7Pv5TSXT8eopQWYMtl98sRU+YCOAlS03F79rXp72VJmv1jVT+fyTXvuADVvoOuB2Do2ENqnx3KYJUoxqe7nq+8P3OnSZUncccmBNBAl1vCeyt4l8BNVsw33d13bg6Wdlocp+/2YavVJXphrdVTPRBmyF3oAP6Ol+y7Jb4FrPuX/74JETKez49by5MdFb4kTUqRyaqQXlue/OCtSBzzay/KDr83Q61GNCuh2fziupTZmhMgSl0PrtqnIgCpzFsodjhQnW43GaQtPErOBTl2tkU2L6keqdNO8u2IOcp8BWrMnAkDdTk66/1jI+5NzAz+M1idKcPFZIKoW/s/s8F/dg7xMcfHyRM4+fjGev5Y4n/cFE+rEFyT19eZFUxv8QsyrdyIM8gNRuBGBFg0PO237+PGysQYmWYWX6fETa3iKSR5h1maEU+QOW2AJg261YGHFpiuyHl2bzFrVG4aLctcFUbZEhkiEl3C8c3uX1F5firTipsB0an6cS2ll/y1wGiB4hioItToGkhq1YWfN5+IW6n7UjEPOGcp7h6ScYcIXFbKhHmWOPnbWHtLZ8NC6gmDBPvXL6NFwgrzOaLr6BEaHVXpu/LlS54auH5SW7s57ql8481aH+Cz2b3F1Y72VQouuL90G7LBzToDrfoC3roOEJzwhNqEihYg78gvm/sQHpkfTl1+mMk4yoPP9Z62vAdx8j3gp8uDCliuKiMkViHEXhs/YOhlwlw0dfe6HIlzvdZneS3P2MGThRbGylBp4Qe53mOV2TzC5+oZkYgK1qPOzKXa9MqxKgx1ixKNvLnjhPFqU8pMuIEWegp7QiMCp5g5YxA2y4SRam4tV8AeRTfl7O6Z/NfImJusRAR2OvtQcYbhzB8izwdYUgZcb1Gf/oQrMz1K322CMsT0nbd5vNgfYdD3IJPUtkFn9GtYXX7/k6OOysXiw=]]></req_info></xml>";
        for (int i=0; i < 5; i++) {
            //weixinPayNotifyController.refund(refundNotify);
        }
    }
}
