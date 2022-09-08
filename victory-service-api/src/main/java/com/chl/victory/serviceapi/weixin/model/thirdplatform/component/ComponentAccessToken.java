package com.chl.victory.serviceapi.weixin.model.thirdplatform.component;

import java.io.Serializable;
import java.util.Date;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class ComponentAccessToken extends BaseResult  implements Serializable {

    /**
     * 第三方平台 access_token
     */
    String component_access_token;
    Long expires_in;

    /**
     * 该token获取的时间，用于判断是否该重新获取
     */
    Date createTime;
}
