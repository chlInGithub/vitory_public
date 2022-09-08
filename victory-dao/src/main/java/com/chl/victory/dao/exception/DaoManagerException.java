package com.chl.victory.dao.exception;

/**
 * 数据访问管理器层异常
 * @author ChenHailong
 * @date 2019/4/30 14:42
 **/
public class DaoManagerException extends Exception {
    public DaoManagerException(String message, Throwable cause) {
        super(message, cause);
    }
    public DaoManagerException(String message) {
        super(message);
    }
    public DaoManagerException(Throwable cause) {
        super(cause);
    }
}
