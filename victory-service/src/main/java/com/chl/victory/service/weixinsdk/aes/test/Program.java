package com.chl.victory.service.weixinsdk.aes.test;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.service.services.weixin.thirdplatform.ComponentService;
import com.chl.victory.service.services.weixin.thirdplatform.event.WXEventService;
import com.chl.victory.service.weixinsdk.WXPayUtil;
import com.chl.victory.service.weixinsdk.aes.WXBizMsgCrypt;
import com.chl.victory.service.weixinsdk.aes.XMLParse;
import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Program {

	public static void main(String[] args) throws Exception {
		testRealData();

	}

	public static void testRealData() throws Exception {
		ComponentService.ComponentConfig componentConfig = new ComponentService.ComponentConfig();
		componentConfig.setComponentAppId("wx2dcbe9091987e73b");
		componentConfig.setEncodingAesKey("XiangHeBoyuKeJiYouXianGongSi12WeiShangCheng");
		componentConfig.setToken("WGlhbmdIZUJveXVLZUppWW91WGlhbkdvbmdTaVdlaVNoYW5nQ2hlbmc=");
		WXBizMsgCrypt pc = new WXBizMsgCrypt(componentConfig.getToken(), componentConfig.getEncodingAesKey(),
				componentConfig.getComponentAppId());
		String msgSignature = "2795b67805639408ee959e36d9c124673460cf03";
		String timestamp = "1591952844";
		String nonce = "1120246460";
		String fromXML = "<xml><AppId><![CDATA[wx2dcbe9091987e73b]]></AppId><Encrypt><![CDATA[C5sJnWi1YjEpizCSFfFn8qs31MzuxT6oeKOXD/hgOuhy4NpDOcIb0Id4SuoExWeVVDhTkVADkh2TnQ/dXVbHk3parkx2fENF7yOQlGhCfNndu3vhVG4yxWzWsA0UN8MRMi/qKEcTRnzpgI7jYQE/7mYOLYpq6q8eKRFHyfXqWiUpetdWVXds88VEM12fBGn3nsH7Mc5opJTJkCNkHAWGgj0Wbf0h5ZepuVrJ3qgHlJWpN2PqpYwJMADmDjtBOhKQz3wjWeFMunJNRViaQZfJwy9liCxGDT/ZRl4vLy95sli40YQ8s0GStxJep7ftJdnIn525Wxgxiat4y2BXHpG6GbJNTVSVxCwwtiEy3qdg7y1UrsOgla+MsiqZBvx4E/q73IRl+gXbtgPepSLmFcBhW6vtuFguRfkaO8E2Swe6klfgKj2K2N7VAUGiTK9ZhMJBWLdwhWZHjw6a0SF7IOuXeg==]]></Encrypt></xml>";
		String result2 = pc.decryptXMLMsg(msgSignature, timestamp, nonce, fromXML);
		System.out.println("解密后明文: " + result2);

		result2 = JSONObject.toJSONString(WXPayUtil.xmlToMap(result2));
		//WXEventService.WXEventDTO wxEventDTO = JSONObject.parseObject(result2, WXEventService.WXEventDTO.class);
		//System.out.println(wxEventDTO);
	}

	public  static void test01() throws Exception {
		//
		// 第三方回复公众平台
		//

		// 需要加密的明文
		String encodingAesKey = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";
		String token = "pamtest";
		String timestamp = "1409304348";
		String nonce = "xxxxxx";
		String appId = "wxb11529c136998cb6";
		String replyMsg = " 中文<xml><ToUserName><![CDATA[oia2TjjewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType><![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Description><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>";

		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
		String mingwen = pc.encryptMsg(replyMsg, timestamp, nonce);
		System.out.println("加密后: " + mingwen);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbf.setXIncludeAware(false);
		dbf.setExpandEntityReferences(false);

		DocumentBuilder db = dbf.newDocumentBuilder();
		StringReader sr = new StringReader(mingwen);
		InputSource is = new InputSource(sr);
		Document document = db.parse(is);

		Element root = document.getDocumentElement();
		NodeList nodelist1 = root.getElementsByTagName("Encrypt");
		NodeList nodelist2 = root.getElementsByTagName("MsgSignature");

		String encrypt = nodelist1.item(0).getTextContent();
		String msgSignature = nodelist2.item(0).getTextContent();

		String format = "<xml><AppId><![CDATA[toUser]]></AppId><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
		String fromXML = String.format(format, encrypt);

		//
		// 公众平台发送消息给第三方，第三方处理
		//

		// 第三方收到公众号平台发送的消息
		String result2 = pc.decryptXMLMsg(msgSignature, timestamp, nonce, fromXML);
		System.out.println("解密后明文: " + result2);

		Object[] extract = XMLParse.extract(fromXML);
		String result3 = pc.decryptMsg(msgSignature, timestamp, nonce, extract[1].toString());
		System.out.println("解密后明文: " + result3);

		//pc.verifyUrl(null, null, null, null);}
	}
}

