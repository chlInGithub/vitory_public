package com.chl.victory.serviceapi.order.enums;

/**
 * 退款单状态，1申请/商家处理中；2商家同意/退款中；3已退款；4拒绝
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum RefundStatusEnum {
    newed(1, "申请/商家处理中")
    ,agree(20, "商家同意/退款中")
    ,applied_for_3th_pay_platform(21, "商家同意/退款中，已向第三方支付平台发起退款申请")
    ,refunded(30, "已退款")
    ,closed(31, "退款关闭")
    ,refuse(40, "拒绝")
    ;

    private Integer code;
    private String desc;

    RefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RefundStatusEnum getByCode(Integer code){
        for (RefundStatusEnum shopActivityStatusEnum : RefundStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
