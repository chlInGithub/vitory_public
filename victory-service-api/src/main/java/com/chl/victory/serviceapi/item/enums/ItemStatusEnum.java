package com.chl.victory.serviceapi.item.enums;

/**
 * Item状态，10:已发布/仓库中;20:准备上架;30:已上架;40:售罄;50:预警
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum ItemStatusEnum {
    published(10, "已发布/仓库中")
    ,willShelf(20, "准备上架")
    ,sale(30, "已上架")
    ,sellOut(40, "售罄")
    ,warn(50, "预警")
    ;

    private Integer code;
    private String desc;

    ItemStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ItemStatusEnum getByCode(Integer code){
        for (ItemStatusEnum shopActivityStatusEnum : ItemStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
