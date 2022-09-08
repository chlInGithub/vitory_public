package com.chl.victory.serviceapi.activity.enums;

/**
 * 活动\优惠状态
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum ActivityConponsStatusEnum {
    invalid(false, 0, "无效")
    ,valid(true, 1, "有效")
    ;

    private Boolean val;
    private Integer code;
    private String desc;

    ActivityConponsStatusEnum(Boolean val, Integer code, String desc) {
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

    public static  ActivityConponsStatusEnum getByVal(Boolean val){
        for (ActivityConponsStatusEnum shopActivityStatusEnum : ActivityConponsStatusEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static  ActivityConponsStatusEnum getByCode(Integer code){
        for (ActivityConponsStatusEnum shopActivityStatusEnum : ActivityConponsStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
