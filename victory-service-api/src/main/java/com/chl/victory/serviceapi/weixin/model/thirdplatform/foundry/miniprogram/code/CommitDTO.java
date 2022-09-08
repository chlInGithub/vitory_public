package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 10:02
 **/
@Data
public class CommitDTO implements Serializable {

    /**
     * 代码库中的代码模版 ID
     */
    @NotEmpty
    String template_id;

    /**
     * 第三方自定义的配置
     * @link https://developers.weixin.qq.com/miniprogram/dev/devtools/ext.html#%E5%B0%8F%E7%A8%8B%E5%BA%8F%E6%A8%A1%E6%9D%BF%E5%BC%80%E5%8F%91
     */
    @NotEmpty
    String ext_json;

    /**
     * 代码版本号，开发者可自定义（长度不要超过 64 个字符）
     */
    @NotEmpty @Size(max = 64)
    String user_version;

    /**
     * 代码描述，开发者可自定义
     */
    @NotEmpty
    String user_desc;

    String time;

    @Data
    public static class ExtJson implements Serializable{
        boolean extEnable = true;
        String extAppid;
        boolean directCommit = true;
        Ext ext;
        @Data
        public static class Ext{
            String shopName;
            String domain;
            String requestDomain;
            String shopId;
            String shopImg;
            String appId;
            Integer tId;

            /**
             * 页面背景色
             */
            String bgColor;

            /**
             * 导航栏背景色
             */
            String ngbgColor;

            /**
             * 导航栏文字色
             */
            String ngFrontColor;
        }
    }
}
