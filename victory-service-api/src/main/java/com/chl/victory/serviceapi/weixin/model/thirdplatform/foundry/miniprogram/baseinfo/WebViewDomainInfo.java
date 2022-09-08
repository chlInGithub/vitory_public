package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * 设置业务域名  结果
 * @author ChenHailong
 * @date 2020/5/27 16:15
 **/
@Data
public class WebViewDomainInfo extends BaseResult  implements Serializable {
    String[] webviewdomain;
}
