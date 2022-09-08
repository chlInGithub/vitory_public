package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class WeixinCode2Session extends BaseResult implements Serializable {

    String openid;

    String unionid;

    String session_key;
}
