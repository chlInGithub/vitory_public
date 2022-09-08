package com.chl.victory.dao.query.merchant;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

@Data
public class MerchantUserQuery extends BaseQuery {

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

}