package com.chl.victory.serviceapi.merchant.enums;

/**
 * 增值能力
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum ShopPowerEnum {
    weiSales((byte)1, "微导购");

    private Byte code;
    private String desc;

    ShopPowerEnum(Byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public Byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ShopPowerEnum getByCode(Integer code){
        for (ShopPowerEnum shopActivityStatusEnum : ShopPowerEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
