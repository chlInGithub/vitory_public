package com.chl.victory.serviceapi.exception;

/**
 * 调用微信接口，没有获取到正确数据的情况
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class WeixinException extends RuntimeException {
    public WeixinException(String message, Throwable cause) {
        super(message, cause);
    }
    public WeixinException(String message) {
        super(message);
    }
    public WeixinException(Throwable cause) {
        super(cause);
    }
}
