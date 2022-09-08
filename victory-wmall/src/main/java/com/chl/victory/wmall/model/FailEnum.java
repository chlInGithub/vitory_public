package com.chl.victory.wmall.model;

/**
 * @author ChenHailong
 * @date 2019/10/30 13:51
 **/
public enum FailEnum {
    NOT_REGISTER(10001, "未注册"),
    NOT_LOGIN(10002, "未登录"),
    NOT_SIGN(10003, "验签失败"),
    ;

    private int type;
    private String msg;

    FailEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
