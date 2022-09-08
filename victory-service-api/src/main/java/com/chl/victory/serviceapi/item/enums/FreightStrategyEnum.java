package com.chl.victory.serviceapi.item.enums;

/**
 * 运费策略 0无运费， 1固定运费，……
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum FreightStrategyEnum {
    ZERO(0, "免运费")
    ,SAME(1, "固定运费")
    ;

    private Integer code;
    private String desc;

    FreightStrategyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FreightStrategyEnum getByCode(Integer code){
        for (FreightStrategyEnum shopActivityStatusEnum : FreightStrategyEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
