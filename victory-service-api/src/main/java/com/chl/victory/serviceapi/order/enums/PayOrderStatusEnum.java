package com.chl.victory.serviceapi.order.enums;

/**
 * 支付单状态，0:新建/待支付；1:已支付
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum PayOrderStatusEnum {
    newed(false, 0, "新建/待支付"),payed(true, 1, "已支付");

    private Boolean val;
    private Integer code;
    private String desc;

    PayOrderStatusEnum(Boolean val, Integer code, String desc) {
        this.val = val;
        this.code = code;
        this.desc = desc;
    }

    public boolean getVal() {
        return val;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PayOrderStatusEnum getByVal(Boolean val){
        for (PayOrderStatusEnum shopActivityStatusEnum : PayOrderStatusEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static PayOrderStatusEnum getByCode(Integer code){
        for (PayOrderStatusEnum shopActivityStatusEnum : PayOrderStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
