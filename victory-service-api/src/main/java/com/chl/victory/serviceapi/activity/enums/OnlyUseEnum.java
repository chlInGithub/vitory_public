package com.chl.victory.serviceapi.activity.enums;

/**
 * 是否排斥其他优惠、活动？0：不排斥，可共同使用；1：排斥，仅单独使用
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum OnlyUseEnum {
    only(true, 1, "仅单独使用"),notOnly(false, 0, "可与其他活动或优惠共同使用");

    private Boolean val;
    private Integer code;
    private String desc;

    OnlyUseEnum(Boolean val, Integer code, String desc) {
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

    public static  OnlyUseEnum getByVal(Boolean val){
        for (OnlyUseEnum shopActivityStatusEnum : OnlyUseEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static  OnlyUseEnum getByCode(Integer code){
        for (OnlyUseEnum shopActivityStatusEnum : OnlyUseEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;    }
}
