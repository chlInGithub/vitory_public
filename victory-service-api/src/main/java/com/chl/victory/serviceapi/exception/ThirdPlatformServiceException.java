package com.chl.victory.serviceapi.exception;

/**
 * (微信)第三方平台 异常
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class ThirdPlatformServiceException extends RuntimeException {
    public ThirdPlatformServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public ThirdPlatformServiceException(String message) {
        super(message);
    }
    public ThirdPlatformServiceException(Throwable cause) {
        super(cause);
    }
}
