package com.chl.victory.web.service;

import com.chl.victory.common.redis.CacheService;
import com.chl.victory.core.rsa.RSACoder;
import com.chl.victory.core.rsa.RSAStrKeyPair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 提供RSA服务
 * <ul>
 *     <li>
 * 维护session : keyPair 关系，超时10s
 *     </li>
 *     <li>
 *         提供RSA服务
 *     </li>
 * </ul>
 */
@Service
public class RSAService {
    @Resource
    CacheService cacheService;

    /**
     * 用于数据库密码的密钥对
     * TODO 应该保护起来
     */
    RSAStrKeyPair _keyPair = new RSAStrKeyPair(
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKZhtXYVow2Lagvd9AEiQq3ceVwBKSSbqwnndDjwfVufwoxYFXpDBApUJQTepMHI8EKAOuCS17cv47vh6zAKxXGycPBDqYQPtmz7b6BwGSG+GP+g/+5z9iXyMEO+SwmfEh4chkrhTw+sUBbhD9vRcZyKC49Bz4EY0s2c/q8yOdHbAgMBAAECgYBdFUMJGlfLhxjAJN+TFtDJJhS7VWboNTL+aoAKLvljHkEgdVMmwIFtKeBAFjOEut83xTerVlva/67f+Se0DAKjiltlQcPf8XGOFPWpLYgv/rodkxHrlcVaqEI8/e6rlWIez6QqxsdXiHoQGLuJhU5Uiz3kFMyzgpGfDKBCApkl4QJBAP5j/6X19ap/LZeVPskhAPQQITrSmru/7mKtTMU35gFp2Q+7gGvMZFRtlypSqgJ6Jx7U6Ii8TbPTfe8RrFv9hbECQQCnbyycxZfXCPy/u5ba0OffFcM8wW1xpM3Yxp6wi/RTUhEh6cusnp3a5n7iZqDW6Ad5XsZ8odYuXkAAtfr9bNdLAkBKndxDurRXUTx3ROCIsDSOYhjNHy8huPdcXEazZmUBryFq+u19MROQrCB12o9hcKD+6yZVzR3hjDxZHk73IJLRAkB6NODhBC+RWhPwdaDj0TqMTzwTKjMRkXJVWJMW1O/dqLashBKOya75yOavpKycbvqVkaFZ8l17tnsMicbKFhkFAkBnIrnEMHvOVr/pAX/7tBJWUczz9YAgpf4ky8CHYKToDDr/uuQqSWli2zMkPlN4fPYtoFYvCI8Nh74rAv1m1zNB",
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmYbV2FaMNi2oL3fQBIkKt3HlcASkkm6sJ53Q48H1bn8KMWBV6QwQKVCUE3qTByPBCgDrgkte3L+O74eswCsVxsnDwQ6mED7Zs+2+gcBkhvhj/oP/uc/Yl8jBDvksJnxIeHIZK4U8PrFAW4Q/b0XGciguPQc+BGNLNnP6vMjnR2wIDAQAB");

    /**
     * 为sessionId生成新的密钥对并返回公钥
     * @param sessionId
     */
    public String genPublicKey(String sessionId){
        RSAStrKeyPair rsaStrKeyPair = RSACoder.genNewKeyPair();
        cacheService.save("privateKey_" + sessionId, rsaStrKeyPair.getPrivateK(), 10);
        return rsaStrKeyPair.getPublicK();
    }
    String getPrivateKey(String sessionId){
        String privateK = cacheService.get("privateKey_" + sessionId);
        return privateK;
    }

    /**
     * 通过SessionId对应的私钥解密
     * @param cryptographic
     * @param sessionId
     * @return
     */
    public String deCrypto(String cryptographic, String sessionId){
        String privateK = getPrivateKey(sessionId);
        String data = RSACoder.decryptByPrivateKey(cryptographic, privateK);
        return data;
    }

    /**
     * 先通过SessionId对应的私钥解密，然后使用另一个公钥加密
     * @param cryptographic
     * @param sessionId
     * @return
     */
    public String deCryptoAndEnCrypto(String cryptographic, String sessionId){
        String privateK = getPrivateKey(sessionId);
        if (null == privateK) {
            return null;
        }
        String data = RSACoder.decryptByPrivateKey(cryptographic, privateK);
        if (null == privateK) {
            return null;
        }
        data = RSACoder.encryptByPublicKey(data, _keyPair.getPublicK());
        return data;
    }

    public String deCrypto(String cryptographic){
        String data = RSACoder.decryptByPrivateKey(cryptographic, _keyPair.getPrivateK());
        return data;
    }
}
