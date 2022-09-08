package com.chl.victory.common.enums;

/**
 * 第三方平台枚举
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum ThirdPlatformEnum {
    weixin(0, "微信"),
    alipay(1, "支付宝"),
    ;

    private Integer code;
    private String desc;

    ThirdPlatformEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ThirdPlatformEnum getByCode(Integer code){
        for (ThirdPlatformEnum shopActivityStatusEnum : values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
