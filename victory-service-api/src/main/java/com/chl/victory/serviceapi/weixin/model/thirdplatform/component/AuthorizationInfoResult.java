package com.chl.victory.serviceapi.weixin.model.thirdplatform.component;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/6/15 10:27
 **/
@Data
public class AuthorizationInfoResult extends BaseResult  implements Serializable {
    AuthorizationInfoDTO authorization_info;
}
