package com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer;

import java.io.Serializable;

import lombok.Data;

/**
 * 素材
 * @author ChenHailong
 * @date 2020/5/28 9:55
 **/
@Data
public class UploadedMediaDTO implements Serializable {

    /**
     * 第三方图片服务器图片ID
     */
    String md5;

    /**
     * 微信素材ID
     */
    String mediaId;
}
