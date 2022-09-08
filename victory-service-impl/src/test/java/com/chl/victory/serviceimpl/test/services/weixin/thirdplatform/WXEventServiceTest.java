package com.chl.victory.serviceimpl.test.services.weixin.thirdplatform;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceimpl.test.BaseTest;
import com.chl.victory.service.services.weixin.thirdplatform.ComponentService;
import com.chl.victory.service.weixinsdk.aes.WXBizMsgCrypt;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/6/15 9:37
 **/
public class WXEventServiceTest extends BaseTest {

    @Test
    public void testDeal() {
        ComponentService.ComponentConfig componentConfig = null;
        String msgSignature = "1d7b2010c6f866f2681fe5edf0747e784936c8ee";
        String timestamp = "1592978881";
        String nonce = "847395684";
        String postdata = "<xml>\n" + "    <AppId><![CDATA[wx2dcbe9091987e73b]]></AppId>\n"
                + "    <Encrypt><![CDATA[E43Itmm9wTfdJXwLHz0LVJ+mKNDYCBbwkPD/QSGmp6E+SOuEtX3dk8HwhX2joWB+9ibA3B84Xz5KTaC0+b0OCeuX86uKJuFG+C2rrN2QkafoKJTVevyIjaq/iPua76IgbLDP6HJ+dlJo8zkYQhB21RV8/akUu0tMVyKziqpGFPrv9+qiOrZu7gQz4juUl+WSZ1RSXeCl6LXm8O1jAzcxmVxYBaA2ofrfeAe8P84tL3VQgsAl4BVneT8D8U8JuKqcD32kLW/lnBCWcgxTal7FQqKqVxobyurJLaaSfSHpt4xCWvMtB/Qo9AltPmVYp54AeJySWPUoiXm+hSzCj4GFRoB+60FxxIvcOMnp31b/8eJaXLuuT8nvg5PHezB/h2M8fOnQcy4QXb2iE+HvOQp/T9XM9zPpicwmNJQzx9JzVUxT1XvTdPuxd7DkRRL+KlemOP2N+I18ybfKk+lMp/FU5RwkYtOWBYk4mTgCGzYiMHXIDMaT36WCAF+FL721SX+rAfNA2hOz4vlI9UgysE3QDNe3/jrZgy+NLXA6Enjq0X7sbdV0FOMSqx4+yPd/WB+095DN/Rs9hlPV8z17rDXFVS87onZ48KHNPujcPr6tXz+EEjH2hNR2UuoUi3N06TnI6+JmLt4yOz4pEB+Bk0UGRnpbpY2OyD3u+98T7BkHF8ne03SF9M/Fjjzovz4hMIPGVAN+NF9nA94QdoR53C2M2gHOcwIL9zY2Lld5AXFcFNAwPx3Qi5K1YHHw0JLcOY8Uig1zASTa+9Ndgi17vudxp4Xb/3UhDr0pmjUGVxIL1pALIuPALhj8RmZsaRCRx/aVq1HI7ZnWF7EHvtL4jK6Y6vBLwHgtiWs5dgS9oR4Y8BuhMFf7Kfql6AS8s6DpSPIE1qRotOWGM7OB3ojyinj6NUEOZbupMaMB+0Rni4prArZvpq8ZRIBNZl+Pwbs1xAcUgX3cdYSImFLYnpa0F1Gs0pTincRnvH4LAtDftRHZQPBmIfyzmCZjq1Il0nzqY31V]]></Encrypt>\n"
                + "</xml>";
        try {
            // String appId = getAppId(postdata);
            componentConfig = ServiceManager.componentService.getComponentConfig();
            //if (componentConfig.getComponentAppId().equals(appId)) {
            // 解密
            WXBizMsgCrypt pc = new WXBizMsgCrypt(componentConfig.getToken(), componentConfig.getEncodingAesKey(),
                    componentConfig.getComponentAppId());
            String content = pc.decryptXMLMsg(msgSignature, timestamp, nonce, postdata);

            ServiceManager.wxEventService.deal(content);
            //}
        } catch (Exception e) {
            System.out.println();
            //log.error("authEvent|{}|{}|{}|{}|{}", msgSignature, timestamp, nonce, postdata, componentConfig, e);
        }

    }

    public static void main(String[] args) {
        String content =
                "<xml><AppId><![CDATA[wx2dcbe9091987e73b]]></AppId>\n" + "<CreateTime>1592978881</CreateTime>\n"
                        + "<InfoType><![CDATA[notify_third_fasteregister]]></InfoType><status>0</status>\n"
                        + "<msg><![CDATA[OK]]></msg>\n" + "<appid><![CDATA[wxb83bf939db53ccb7]]></appid>\n"
                        + "<auth_code><![CDATA[queryauthcode@@@VTlA_R8pzyTq4LhOkyRcGGQhLUYzf7v6wB1LsoqGhnK-X74yF-TFzlaQBrHE6pDx8GWigVWZyHs5Ze6P2Rsahw]]></auth_code>\n"
                        + "<info>\n" + "<name><![CDATA[香河博予科技有限公司]]></name>\n"
                        + "<code><![CDATA[91131024MA0D4UD80L]]></code>\n" + "<code_type>1</code_type>\n"
                        + "<legal_persona_wechat><![CDATA[HenrySusie]]></legal_persona_wechat>\n"
                        + "<legal_persona_name><![CDATA[陈海龙]]></legal_persona_name>\n"
                        + "<component_phone><![CDATA[18500425785]]></component_phone>\n" + "</info>\n" + "</xml>";
        //WXEventService.WXEventDTO wxEventDTO = XmlUtil.fromXml(content, "xml", WXEventService.WXEventDTO.class);
        //WXEventHandler4Fasteregister.WXEventDTO wxEventDTO1 = XmlUtil.fromXml(content, "xml", WXEventHandler4Fasteregister.WXEventDTO.class);

        /*XStream xStream = new XStream(new StaxDriver());
        xStream.alias("xml", WXEventService.WXEventDTO.class);
        xStream.ignoreUnknownElements();
        WXEventService.WXEventDTO o = (WXEventService.WXEventDTO)xStream.fromXML(content);*/
        System.out.println();
    }
}
