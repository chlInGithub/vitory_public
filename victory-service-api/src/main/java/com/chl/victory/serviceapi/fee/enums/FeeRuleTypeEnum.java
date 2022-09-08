package com.chl.victory.serviceapi.fee.enums;

/**
 * 佣金类型
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum FeeRuleTypeEnum {
    amount(1, "固定金额")
    ,order_realfee_ratio(2, "订单金额比例")
    ;

    private Integer code;
    private String desc;

    FeeRuleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FeeRuleTypeEnum getByCode(Integer code){
        for (FeeRuleTypeEnum feeRuleTypeEnum : FeeRuleTypeEnum.values()) {
            if (feeRuleTypeEnum.code.equals(code)){
                return feeRuleTypeEnum;
            }
        }
        return null;
    }
}
