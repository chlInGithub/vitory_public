package com.chl.victory.serviceapi.order.enums;

/**
 * 支付类型 0 wechart; 1 alipay
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum PayTypeEnum {
    wechart(0, "微信支付")
    ,alipay(1, "支付宝")
    ,offline(2, "线下支付")
    ;

    private Integer code;
    private String desc;

    PayTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PayTypeEnum getByCode(Integer code){
        for (PayTypeEnum shopActivityStatusEnum : PayTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
