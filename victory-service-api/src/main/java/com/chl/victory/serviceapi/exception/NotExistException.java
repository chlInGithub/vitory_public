package com.chl.victory.serviceapi.exception;

/**
 * 业务逻辑层异常
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class NotExistException extends RuntimeException {
    public NotExistException(String message, Throwable cause) {
        super(message, cause);
    }
    public NotExistException(String message) {
        super(message);
    }
    public NotExistException(Throwable cause) {
        super(cause);
    }
}
