package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class MerchantUserDTO extends BaseDTO implements Serializable {
    /**
     * 描述
     * desc
     */
    private String desc;

    /**
     * 手机号
     * mobile
     */
    private Long mobile;

    /**
     * 密码，非对称加密后密文
     * pass
     */
    private String pass;

    /**
     * 用户可用？0：不可用；1：可用
     * valid
     * @see com.chl.victory.serviceapi.merchant.enums.MerchantValidEnum
     */
    private Boolean valid;

    /**
     * 已修改初始密码？0：未修改；1：已修改。要求修改初始密码后，才可进行业务操作
     * init_pass_modified
     * @see com.chl.victory.serviceapi.merchant.enums.MerchantModifyInitPassEnum
     */
    private Boolean initPassModified;


    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public void setPass(String pass) {
        this.pass = pass == null ? null : pass.trim();
    }

}