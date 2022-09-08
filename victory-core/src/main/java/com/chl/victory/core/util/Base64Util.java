package com.chl.victory.core.util;

import org.apache.commons.codec.binary.Base64;

/**
 * @author ChenHailong
 * @date 2019/4/22 12:22
 **/
public class Base64Util {

    public static byte[] decryptBASE64Bytes(String str) {
        return Base64.decodeBase64(str);
    }
    /**
     * base64Str to str
     */
    public static String decryptBASE64Str(String str) {
        return new String(Base64.decodeBase64(str));
    }


    public static String encryptBASE64Bytes(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
    /**
     * str to base64Str
     */
    public static String encryptBASE64Str(String base64Str) {
        return Base64.encodeBase64String(base64Str.getBytes());
    }

}
