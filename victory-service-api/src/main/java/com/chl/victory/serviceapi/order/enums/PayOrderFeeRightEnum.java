package com.chl.victory.serviceapi.order.enums;

/**
 * 支付单检查支付金额对吗？0：不对；1：对
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum PayOrderFeeRightEnum {
    wrong(false, 0, "支付金额不正确"),right(true, 1, "支付金额正确");

    private Boolean val;
    private Integer code;
    private String desc;

    PayOrderFeeRightEnum(Boolean val, Integer code, String desc) {
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

    public static PayOrderFeeRightEnum getByVal(Boolean val){
        for (PayOrderFeeRightEnum shopActivityStatusEnum : PayOrderFeeRightEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static PayOrderFeeRightEnum getByCode(Integer code){
        for (PayOrderFeeRightEnum shopActivityStatusEnum : PayOrderFeeRightEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
