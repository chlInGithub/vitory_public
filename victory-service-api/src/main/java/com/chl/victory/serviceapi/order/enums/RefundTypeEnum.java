package com.chl.victory.serviceapi.order.enums;

/**
 * 退款类型，1退货退款  2仅退款   3换货
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum RefundTypeEnum {
    all(1, "退货退款")
    ,onlyMoney(2, "仅退款")
    ,onlyGoods(3, "换货")
    ;

    private Integer code;
    private String desc;

    RefundTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RefundTypeEnum getByCode(Integer code){
        for (RefundTypeEnum shopActivityStatusEnum : RefundTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
