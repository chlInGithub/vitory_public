package com.chl.victory.serviceapi.exception;

/**
 * 到达限流阈值时抛出的异常
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class AccessLimitException extends RuntimeException {
    public AccessLimitException() {
        super("操作频率过高，请稍后再访问。");
    }
    public AccessLimitException(String erorMsg) {
        super(erorMsg);
    }
}
