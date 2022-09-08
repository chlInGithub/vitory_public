package com.chl.victory.core.rsa;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.chl.victory.core.util.Base64Util.decryptBASE64Bytes;
import static com.chl.victory.core.util.Base64Util.encryptBASE64Bytes;

/**
 * RSA非对称加密
 */
public class RSACoder {
     static final String KEY_ALGORITHM = "RSA";
     static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       加密数据
     * @param privateKey 私钥
     * @return
     */
    public static String sign(byte[] data, String privateKey){
        try{
            // 解密由base64编码的私钥
            byte[] keyBytes = decryptBASE64Bytes(privateKey);
            // 构造PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            // KEY_ALGORITHM 指定的加密算法
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            // 取私钥匙对象
            PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
            // 用私钥对信息生成数字签名
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(priKey);
            signature.update(data);
            return encryptBASE64Bytes(signature.sign());
        }catch (Exception e){
        }
        return null;
    }

    /**
     * 校验数字签名
     *
     * @param data      加密数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return 校验成功返回true 失败返回false
     */
    public static boolean verify(byte[] data, String publicKey, String sign){
        try{
            // 解密由base64编码的公钥
            byte[] keyBytes = decryptBASE64Bytes(publicKey);
            // 构造X509EncodedKeySpec对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            // KEY_ALGORITHM 指定的加密算法
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            // 取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(pubKey);
            signature.update(data);
            // 验证签名是否正常
            return signature.verify(decryptBASE64Bytes(sign));
        }catch (Exception e){
        }
        return false;
    }

    /**
     * 用私钥解密
     * @param data base64形式密文的byte[]
     * @param privateKey 私钥
     * @return 解密后明文的byte[]
     */
    static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception{
        // 对密钥解密
        byte[] keyBytes = decryptBASE64Bytes(privateKey);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key _privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, _privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 用私钥解密
     * @param data base64形式密文
     * @param privateKey 私钥
     * @return 解密后明文
     */
    public static String decryptByPrivateKey(String data, String privateKey){
        try {
            return new String(decryptByPrivateKey(decryptBASE64Bytes(data),privateKey));
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 用公钥加密
     *
     * @param data 待加密明文byte[]
     * @param key 公钥
     * @return 加密后byte[]
     * @throws Exception
     */
    static byte[] encryptByPublicKey(byte[] data, String key)
            throws Exception {
        // 对公钥解密
        byte[] keyBytes = decryptBASE64Bytes(key);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 用公钥加密
     *
     * @param data 待加密明文
     * @param key 公钥
     * @return 加密后base64形式
     */
    public static String encryptByPublicKey(String data, String key){
        try {
            return encryptBASE64Bytes(encryptByPublicKey(data.getBytes(), key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成一对新秘钥
     *
     * @throws Exception
     */
    public static RSAStrKeyPair genNewKeyPair(){
        try{
            KeyPairGenerator keyPairGen = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(1024);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            String publicK = encryptBASE64Bytes(keyPair.getPublic().getEncoded());
            String privateK = encryptBASE64Bytes(keyPair.getPrivate().getEncoded());
            return new RSAStrKeyPair(privateK, publicK);
        }catch (Exception e){
        }
        return null;
    }
}
