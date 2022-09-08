package com.chl.victory.core.rsa;

import lombok.Data;

/**
 * 字符串形式的密钥对
 * @author ChenHailong
 * @date 2019/4/22 10:13
 **/
@Data
public class RSAStrKeyPair {
    String privateK;
    String publicK;

    public RSAStrKeyPair(String privateK, String publicK) {
        this.privateK = privateK;
        this.publicK = publicK;
    }
}
