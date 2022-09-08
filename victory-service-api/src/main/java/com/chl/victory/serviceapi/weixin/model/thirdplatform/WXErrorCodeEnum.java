package com.chl.victory.serviceapi.weixin.model.thirdplatform;

/**
 * https://developers.weixin.qq.com/doc/oplatform/Return_codes/Return_code_descriptions_new.html
 * @author ChenHailong
 * @date 2019/5/8 8:57
 **/
public enum WXErrorCodeEnum {
    UNIFIED_SOCIAL_CREDIT_CODE(1, "统一社会信用代码(18位)")
    ,ORGANIZATION_CODE(2, "组织机构代码(9位)")
    ,BUSINESS_LICENSE_REGISTRATION_NO(3, "营业执照注册号(15位)")
        ;
    private Integer type;
    private String desc;

    WXErrorCodeEnum(Integer type, String desc) {
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
