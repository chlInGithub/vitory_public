package com.chl.victory.core.rsa;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RSACoderTest {
    private String data;
    @Before
    public void init(){
        data = "235DWHDFI34234";
    }



    @Test
    public void test_GenKeyPair(){
        RSAStrKeyPair _keyPair = new RSAStrKeyPair(
                "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKZhtXYVow2Lagvd9AEiQq3ceVwBKSSbqwnndDjwfVufwoxYFXpDBApUJQTepMHI8EKAOuCS17cv47vh6zAKxXGycPBDqYQPtmz7b6BwGSG+GP+g/+5z9iXyMEO+SwmfEh4chkrhTw+sUBbhD9vRcZyKC49Bz4EY0s2c/q8yOdHbAgMBAAECgYBdFUMJGlfLhxjAJN+TFtDJJhS7VWboNTL+aoAKLvljHkEgdVMmwIFtKeBAFjOEut83xTerVlva/67f+Se0DAKjiltlQcPf8XGOFPWpLYgv/rodkxHrlcVaqEI8/e6rlWIez6QqxsdXiHoQGLuJhU5Uiz3kFMyzgpGfDKBCApkl4QJBAP5j/6X19ap/LZeVPskhAPQQITrSmru/7mKtTMU35gFp2Q+7gGvMZFRtlypSqgJ6Jx7U6Ii8TbPTfe8RrFv9hbECQQCnbyycxZfXCPy/u5ba0OffFcM8wW1xpM3Yxp6wi/RTUhEh6cusnp3a5n7iZqDW6Ad5XsZ8odYuXkAAtfr9bNdLAkBKndxDurRXUTx3ROCIsDSOYhjNHy8huPdcXEazZmUBryFq+u19MROQrCB12o9hcKD+6yZVzR3hjDxZHk73IJLRAkB6NODhBC+RWhPwdaDj0TqMTzwTKjMRkXJVWJMW1O/dqLashBKOya75yOavpKycbvqVkaFZ8l17tnsMicbKFhkFAkBnIrnEMHvOVr/pAX/7tBJWUczz9YAgpf4ky8CHYKToDDr/uuQqSWli2zMkPlN4fPYtoFYvCI8Nh74rAv1m1zNB",
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmYbV2FaMNi2oL3fQBIkKt3HlcASkkm6sJ53Q48H1bn8KMWBV6QwQKVCUE3qTByPBCgDrgkte3L+O74eswCsVxsnDwQ6mED7Zs+2+gcBkhvhj/oP/uc/Yl8jBDvksJnxIeHIZK4U8PrFAW4Q/b0XGciguPQc+BGNLNnP6vMjnR2wIDAQAB");
        System.out.println(RSACoder.encryptByPublicKey("12345674567",_keyPair.getPublicK()));
        System.out.println(RSACoder.encryptByPublicKey("abc123", _keyPair.getPublicK()));
        System.out.println(RSACoder.encryptByPublicKey("12345674567",_keyPair.getPublicK()));
        System.out.println(RSACoder.encryptByPublicKey("abc123", _keyPair.getPublicK()));
    }

    @Test
    public void test(){
        RSAStrKeyPair rsaStrKeyPair = RSACoder.genNewKeyPair();
        String enResult = RSACoder.encryptByPublicKey(data, rsaStrKeyPair.getPublicK());
        System.out.println(enResult);
        String deResult = RSACoder.decryptByPrivateKey(enResult, rsaStrKeyPair.getPrivateK());
        Assert.assertEquals(deResult, data);

        RSAStrKeyPair rsaStrKeyPair1 = RSACoder.genNewKeyPair();
        String enResult1 = RSACoder.encryptByPublicKey(data, rsaStrKeyPair1.getPublicK());
        System.out.println(enResult1);
        String deResult1 = RSACoder.decryptByPrivateKey(enResult1, rsaStrKeyPair.getPrivateK());
        System.out.println(deResult1);
        Assert.assertNull(deResult1);
    }

}
