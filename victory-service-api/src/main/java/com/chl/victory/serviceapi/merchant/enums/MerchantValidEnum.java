package com.chl.victory.serviceapi.merchant.enums;

/**
 * 用户可用？0：不可用；1：可用
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum MerchantValidEnum {
    valid(true, 1, "可用"),invalid(false, 0, "不可用");

    private Boolean val;
    private Integer code;
    private String desc;

    MerchantValidEnum(Boolean val, Integer code, String desc) {
        this.val = val;
        this.code = code;
        this.desc = desc;
    }

    public Boolean getVal() {
        return val;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MerchantValidEnum getByVal(Boolean val){
        for (MerchantValidEnum shopActivityStatusEnum : MerchantValidEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static MerchantValidEnum getByCode(Integer code){
        for (MerchantValidEnum shopActivityStatusEnum : MerchantValidEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;    }
}
