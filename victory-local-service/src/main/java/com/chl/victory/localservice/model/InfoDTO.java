package com.chl.victory.localservice.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 通知消息
 * @author ChenHailong
 * @date 2019/5/28 13:23
 **/
@Data
public class InfoDTO  implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String url;
    private String urlDesc;

    /**
     * 1:已读，0:未读
     */
    private Integer see = 0;
}
