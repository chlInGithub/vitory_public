package com.chl.victory.serviceapi.shorturl.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/3 14:12
 **/
@Data
public class ShortUrlDTO implements Serializable {

    /**
     * 用途类型
     * com.chl.victory.service.model.shorturl.ShortUrlDTO.ModelType
     */
    Integer type;
    Long shopId;
    Long userId;
    Long itemId;
    String itemTitle;
    String itemImg;
    Date time;

    public enum ModelType{
        share_item(1), share_shop(2);
        Integer code;

        ModelType(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }
}
