package com.chl.victory.common.enums.merchant;

/**
 * 销售策略
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum SaleStrategyTypeEnum {
    preSale((byte)1, "预售", DealPointEnum.beforeGenOrder.getCode()),
    orderMinFee((byte)2, "订单优惠后金额下限", DealPointEnum.afterRealFee.getCode()),
    minCount((byte)3, "限购-最小购买量", DealPointEnum.beforeGenOrder.getCode()),
    maxCount((byte)4, "限购-最大购买量", DealPointEnum.beforeGenOrder.getCode()),
    ;

    private Byte code;
    private String desc;

    /**
     * @see DealPointEnum
     */
    private Integer dealPoint;

    SaleStrategyTypeEnum(Byte code, String desc, Integer dealPoint) {
        this.code = code;
        this.desc = desc;
        this.dealPoint = dealPoint;
    }

    public Byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getDealPoint() {
        return dealPoint;
    }

    public static SaleStrategyTypeEnum getByCode(Byte code){
        for (SaleStrategyTypeEnum shopActivityStatusEnum : SaleStrategyTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
