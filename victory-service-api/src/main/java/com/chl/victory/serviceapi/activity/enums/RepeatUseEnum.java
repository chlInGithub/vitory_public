package com.chl.victory.serviceapi.activity.enums;

/**
 *  同一订单重复使用，如每100减10，则200减20？0：不可以；1：可以
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum RepeatUseEnum {
    repeat(true, 1, "可多次使用"),notRepeat(false, 0, "仅使用一次");

    private Boolean val;
    private Integer code;
    private String desc;

    RepeatUseEnum(Boolean val, Integer code, String desc) {
        this.val = val;
        this.code = code;
        this.desc = desc;
    }

    public boolean getVal() {
        return val;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static  RepeatUseEnum getByVal(Boolean val){
        for (RepeatUseEnum shopActivityStatusEnum : RepeatUseEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static  RepeatUseEnum getByCode(Integer code){
        for (RepeatUseEnum shopActivityStatusEnum : RepeatUseEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
