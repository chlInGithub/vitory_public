package com.chl.victory.serviceapi.share.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/28 10:11
 **/
@Data
public class ShareDTO implements Serializable {

    /**
     * 生成的分享图片，如小程序码
     */
    private byte[] imgBytes;

    /**
     * 分享的短字符串，可作为短链接内容
     */
    private String shortStr;

    private Long shopId;
    private String shopName;

    private Long userId;

    private String appId;

    private Long itemId;

    private String shopImg;

    private String itemImg;

    private String itemTitle;

    private String nick;
    private String userImg;

}
