package com.chl.victory.serviceapi.merchant.enums;

/**
 * 已修改初始密码？0：未修改；1：已修改。要求修改初始密码后，才可进行业务操作
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum MerchantModifyInitPassEnum {
    modified(true, 1, "已修改。要求修改初始密码后，才可进行业务操作"),notModified(false, 0, "未修改");

    private Boolean val;
    private Integer code;
    private String desc;

    MerchantModifyInitPassEnum(Boolean val, Integer code, String desc) {
        this.val = val;
        this.code = code;
        this.desc = desc;
    }

    public Boolean getVal() {
        return val;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MerchantModifyInitPassEnum getByVal(Boolean val){
        for (MerchantModifyInitPassEnum shopActivityStatusEnum : MerchantModifyInitPassEnum.values()) {
            if (shopActivityStatusEnum.val.equals(val)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }

    public static MerchantModifyInitPassEnum getByCode(Integer code){
        for (MerchantModifyInitPassEnum shopActivityStatusEnum : MerchantModifyInitPassEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;    }
}
