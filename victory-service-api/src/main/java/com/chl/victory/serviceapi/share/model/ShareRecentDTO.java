package com.chl.victory.serviceapi.share.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 分享内容的近况，如统计
 * @author ChenHailong
 * @date 2020/12/18 14:34
 **/
@Data
public class ShareRecentDTO implements Serializable {

    /**
     * 分享的近似时间
     */
    String time;

    /**
     * 分享对应的短码
     */
    String scene;

    /**
     * 引流新用户的数量
     */
    Long gainCount;
}
