package com.chl.victory.serviceapi.fee.enums;

/**
 * 佣金记录状态
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum FeeStatusEnum {
    newed(1, "待审核")
    ,checked(2, "待支付")
    ,payed(3, "已支付")
    ;

    private Integer code;
    private String desc;

    FeeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FeeStatusEnum getByCode(Integer code){
        for (FeeStatusEnum feeStatusEnum : FeeStatusEnum.values()) {
            if (feeStatusEnum.code.equals(code)){
                return feeStatusEnum;
            }
        }
        return null;
    }
}
