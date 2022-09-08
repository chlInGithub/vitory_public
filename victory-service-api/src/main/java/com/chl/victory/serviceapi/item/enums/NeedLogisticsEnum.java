package com.chl.victory.serviceapi.item.enums;

/**
 * 需要物流，0不需要， 1 需要
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum NeedLogisticsEnum {
    NOT_NEED(0, "不需要")
    ,NEED(1, "需要")
    ;

    private Integer code;
    private String desc;

    NeedLogisticsEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static NeedLogisticsEnum getByCode(Integer code){
        for (NeedLogisticsEnum shopActivityStatusEnum : NeedLogisticsEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
