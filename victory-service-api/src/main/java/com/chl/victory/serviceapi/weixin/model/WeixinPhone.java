package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/12/20 17:25
 **/
@Data
public class WeixinPhone  implements Serializable {

    /**
     * 没有区号的手机号
     */
    String purePhoneNumber;

    /**
     * 区号
     */
    String countryCode;
}
