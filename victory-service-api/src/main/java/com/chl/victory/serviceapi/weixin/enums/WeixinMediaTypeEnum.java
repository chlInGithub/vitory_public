package com.chl.victory.serviceapi.weixin.enums;

/**
 * @author ChenHailong
 * @date 2020/8/31 18:53
 **/
public enum WeixinMediaTypeEnum {
    image(1, "image"), voice(2, "voice"), video(3, "video"), thumb(4, "thumb");

    private Integer type;

    private String desc;

    WeixinMediaTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getType() {
        return type;
    }
}
