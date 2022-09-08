package com.chl.victory.common.enums;

/**
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum YesNoEnum {
    Yes(1, "是")
    ,No(0, "否")
    ;

    private Integer code;
    private String desc;

    YesNoEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static YesNoEnum getByCode(Integer code){
        for (YesNoEnum yesNoEnum : values()) {
            if (yesNoEnum.code.equals(code)){
                return yesNoEnum;
            }
        }
        return null;
    }
}
