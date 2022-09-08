package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import lombok.Data;

/**
 * 获取小程序码
 *
 * @author ChenHailong
 * @date 2019/12/20 17:25
 *
 * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html
 **/
@Data
public class WeixinACodeParam implements Serializable {

    /**
     * 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~
     */
    String scene;

    /**
     * 必须是已经发布的小程序存在的页面（否则报错），例如 pages/index/index, 根路径前不要填加 /,不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
     */
    String page;

    /**
     * 二维码的宽度，单位 px，最小 280px，最大 1280px
     */
    Integer width;

    ShortUrlDTO shortUrlDTO;
}
