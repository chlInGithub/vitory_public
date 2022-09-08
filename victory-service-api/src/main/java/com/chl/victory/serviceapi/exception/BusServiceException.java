package com.chl.victory.serviceapi.exception;

/**
 * 业务逻辑层异常
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class BusServiceException extends Exception {
    public BusServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public BusServiceException(String message) {
        super(message);
    }
    public BusServiceException(Throwable cause) {
        super(cause);
    }
}
