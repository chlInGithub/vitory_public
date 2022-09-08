package com.chl.victory.service.utils;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.serviceapi.weixin.model.WeixinPhone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author ChenHailong
 * @date 2019/12/18 15:49
 **/
@Slf4j
public class WeixinMiniProgramUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 解密微信手机号
     * @param accessKey 可从session中获取
     * @param encryptedData 来自微信接口，手机号信息密文
     * @param ivParam 来自微信接口，加密算法初始向量
     * @return
     */
    public static WeixinPhone getWeixinPhone(String accessKey, String encryptedData, String ivParam) {
        WeixinPhone weixinPhone = null;
        try {
            byte[] content = Base64.decodeBase64(encryptedData);
            byte[] keyByte = Base64.decodeBase64(accessKey);
            byte[] ivByte = Base64.decodeBase64(ivParam);

            Security.addProvider(new BouncyCastleProvider());

            AlgorithmParameters iv = AlgorithmParameters.getInstance("AES");
            iv.init(new IvParameterSpec(ivByte));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            byte[] decrypted = cipher.doFinal(content);

            int pad = decrypted[ decrypted.length - 1 ];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }

            decrypted = Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);

            String result = new String(decrypted);

            weixinPhone = JSONObject.parseObject(result, WeixinPhone.class);
        } catch (Exception e) {
            log.error("解析微信手机号", e);
        }

        return weixinPhone;
    }

    /**
     * @param keyMD5 key的MD5 32个字符的小写形式
     * @param encryptedNotifyContent 密文，base64形式
     * @return
     * @throws Exception
     */
    public static String getRefundNotifyContent(String keyMD5, String encryptedNotifyContent) throws Exception {
        // 创建密码器
        String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";
        String ALGORITHM = "AES";
        try {
            byte[] keyMD5Bytes = keyMD5.getBytes("UTF-8");
            SecretKeySpec key = new SecretKeySpec(keyMD5Bytes, ALGORITHM);

            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key);

            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            String newData = new String(decoder.decode(encryptedNotifyContent), "ISO-8859-1");

            return new String(cipher.doFinal(newData.getBytes("ISO-8859-1")), "utf-8");
        } catch (Exception e) {
            throw new Exception("解密退款notify报文异常", e);
        }
    }
}


