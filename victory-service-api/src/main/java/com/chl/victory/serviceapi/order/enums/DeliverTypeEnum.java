package com.chl.victory.serviceapi.order.enums;

/**
 * 配送方式,0:不需要物流;1:需要物流
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum DeliverTypeEnum {
    noLogistics(0, "不需要物流")
    ,logistics(1, "需要物流")
    ;

    private Integer code;
    private String desc;

    DeliverTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DeliverTypeEnum getByCode(Integer code){
        for (DeliverTypeEnum shopActivityStatusEnum : DeliverTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
