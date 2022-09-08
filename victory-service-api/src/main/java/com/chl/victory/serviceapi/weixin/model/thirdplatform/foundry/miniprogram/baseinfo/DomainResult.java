package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/24 19:52
 **/
@Data
public class DomainResult  implements Serializable {
    DomainInfo domainInfo;
    WebViewDomainInfo webViewDomainInfo;
}
