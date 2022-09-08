package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * 设置服务器域名  结果
 * @author ChenHailong
 * @date 2020/5/27 16:15
 **/
@Data
public class DomainInfo extends BaseResult  implements Serializable {
    String[] requestdomain;
    String[] wsrequestdomain;
    String[] uploaddomain;
    String[] downloaddomain;
}
