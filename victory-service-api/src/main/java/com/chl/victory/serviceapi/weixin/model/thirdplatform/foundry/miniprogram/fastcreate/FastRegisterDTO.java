package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 14:17
 **/
@Data
public class FastRegisterDTO implements Serializable {

    /**
     * 企业名（需与工商部门登记信息一致）
     */
    @NotEmpty
    String name;

    /**
     * 企业代码
     */
    @NotEmpty
    String code;

    /**
     * @see {@link com.chl.victory.serviceapi.weixin.model.thirdplatform.EnterpriseCodeTypeEnum}
     * 企业代码类型 1：统一社会信用代码（18 位） 2：组织机构代码（9 位 xxxxxxxx-x） 3：营业执照注册号(15 位)
     */
    @NotNull
    Integer code_type;

    /**
     * 法人微信号
     */
    @NotEmpty
    String legal_persona_wechat;

    /**
     * 法人姓名（绑定银行卡）
     */
    @NotEmpty
    String legal_persona_name;

    /**
     * 第三方联系电话（方便法人与第三方联系）
     */
    @NotEmpty
    String component_phone;
}
