package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 9:36
 **/
@Data
public class ReleaseResult extends BaseResult implements Serializable {
    /**
     * 小程序版本说明和功能解释
     */
    String version_desc;

    String time;
}
