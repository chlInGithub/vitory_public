package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/31 17:31
 **/
@Data
public class WxPhoneDTO  implements Serializable {
    @NotNull
    Long shopId;
    @NotNull
    Long userId;
    @NotEmpty
    String appId;
    @NotNull
    Long tId;

    String currentMobile;

    @NotEmpty
    String sessionKey;
    @NotEmpty
    String encryptedData;

    @NotEmpty
    String iv;
}
