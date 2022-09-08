package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 10:02
 **/
@Data
public class SubmitAuditDTO implements Serializable {

    /**
     * 小程序版本说明和功能解释
     */
    String version_desc;

    /**
     * 预览信息（小程序页面截图）
     */
    PreviewInfo preview_info;


    @Data
    public static class PreviewInfo implements Serializable{

        /**
         * 截屏mediaid列表，可以通过提审素材上传接口获得
         */
        String[] pic_id_list;
    }
}
