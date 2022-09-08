package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 9:36
 **/
@Data
public class GetTesterResult extends BaseResult implements Serializable {
    Tester[] members;
    @Data
    public static class Tester implements Serializable{
        /**
         * 人员对应的唯一字符串
         */
        String userstr;
        String wechatId;
    }
}
