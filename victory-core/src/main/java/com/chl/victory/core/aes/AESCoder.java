package com.chl.victory.core.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author ChenHailong
 * @date 2020/10/10 17:14
 **/
public class AESCoder {
    /**
     * 加密
     * @param content 待加密内容
     * @param secretKeyStr 加密使用的 AES 密钥，BASE64 编码后的字符串
     * @param iv 初始化向量，长度为 16 个字节，16*8 = 128 位
     * @return 加密后的密文,进行 BASE64 处理之后返回
     */
    public static String encryptAES(byte[] content, String secretKeyStr, String iv)
            throws Exception {
        // 获得一个 SecretKeySpec
        // SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKeyStr), "AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyStr.getBytes(), "AES");
        // 获得加密算法实例对象 Cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //"算法/模式/补码方式"
        // 获得一个 IvParameterSpec
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());  // 使用 CBC 模式，需要一个向量 iv, 可增加加密算法的强度
        // 根据参数初始化算法
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        // 执行加密并返回经 BASE64 处助理之后的密文
        return new Base64().encodeToString(cipher.doFinal(content));
    }

    /**
     * 解密
     * @param content: 待解密内容，是 BASE64 编码后的字节数组
     * @param secretKeyStr: 解密使用的 AES 密钥，BASE64 编码后的字符串
     * @param iv: 初始化向量，长度 16 字节，16*8 = 128 位
     * @return 解密后的明文，直接返回经 UTF-8 编码转换后的明文
     */
    public static String decryptAES(byte[] content, String secretKeyStr, String iv) throws Exception {
        // 密文进行 BASE64 解密处理
        byte[] contentDecByBase64 = new Base64().decode(content);
        // 获得一个 SecretKeySpec
        // SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKeyStr), "AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyStr.getBytes(), "AES");
        // 获得加密算法实例对象 Cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //"算法/模式/补码方式"
        // 获得一个初始化 IvParameterSpec
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        // 根据参数初始化算法
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        // 解密
        return new String(cipher.doFinal(contentDecByBase64), "utf8");
    }

        public static void main(String[] args) throws Exception {

        String key = "wx66e252cb46abe81signsignsignsig";
        String ivStr = "wx66e252cb46abe8";
        byte[] content = "appId=wx66e252cb46abe8e4&code=051H8Tkl2BkeM54AWfml2k4lTz4H8Tks&requestDomain=wmall.5jym.com&shopId=1&tId=0&t=1602483847".getBytes();

            String encryptAES = encryptAES(content, key, ivStr);
            System.out.println(encryptAES);

            String decryptAES = decryptAES(encryptAES.getBytes(), key, ivStr);
            System.out.println(decryptAES);
            // 设置加密模式为AES的CBC模式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

        // 加密
        byte[] encrypted = cipher.doFinal(content);

        // 使用BASE64对加密后的字符串进行编码
        String base64Encrypted = new Base64().encodeToString(encrypted);

        System.out.println(base64Encrypted);
    }
}
