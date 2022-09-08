package com.chl.victory.common.enums.merchant;

/**
 * 执行点
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum DealPointEnum {
    beforeGenOrder(1, "生成订单之前"),
    afterRealFee(10, "计算实付金额之后"),
    ;

    private Integer code;
    private String desc;

    DealPointEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DealPointEnum getByCode(Integer code){
        for (DealPointEnum shopActivityStatusEnum : DealPointEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
