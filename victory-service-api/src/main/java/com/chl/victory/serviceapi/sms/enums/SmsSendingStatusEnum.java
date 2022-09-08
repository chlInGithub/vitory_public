package com.chl.victory.serviceapi.sms.enums;

/**
 * 短信发送结果状态，1:新建；2成功；3失败
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum SmsSendingStatusEnum {
    sending(1, "新建/发送中")
    ,success(2, "成功")
    ,fail(3, "失败")
    ;

    private Integer code;
    private String desc;

    SmsSendingStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SmsSendingStatusEnum getByCode(Integer code){
        for (SmsSendingStatusEnum shopActivityStatusEnum : SmsSendingStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
