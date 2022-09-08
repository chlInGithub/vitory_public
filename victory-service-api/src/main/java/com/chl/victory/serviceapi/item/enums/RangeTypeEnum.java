package com.chl.victory.serviceapi.item.enums;

/**
 * 商品分类
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum RangeTypeEnum {
    recommend(2, "推荐商品")
    ,lowPrice(3, "低价商品")
    ;

    private Integer code;
    private String desc;

    RangeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RangeTypeEnum getByCode(Integer code){
        for (RangeTypeEnum shopActivityStatusEnum : RangeTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
