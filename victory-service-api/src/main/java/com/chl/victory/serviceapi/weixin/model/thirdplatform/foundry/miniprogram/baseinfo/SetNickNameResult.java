package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 16:39
 **/
@Data
public class SetNickNameResult extends BaseResult  implements Serializable {
    String wording;
    Long audit_id;
}
