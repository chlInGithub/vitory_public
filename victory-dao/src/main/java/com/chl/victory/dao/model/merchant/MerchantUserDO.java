package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;

public class MerchantUserDO extends BaseDO {
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
     */
    private Boolean valid;

    /**
     * 已修改初始密码？0：未修改；1：已修改。要求修改初始密码后，才可进行业务操作
     * init_pass_modified
     */
    private Boolean initPassModified;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public Long getMobile() {
        return mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass == null ? null : pass.trim();
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean getInitPassModified() {
        return initPassModified;
    }

    public void setInitPassModified(Boolean initPassModified) {
        this.initPassModified = initPassModified;
    }
}