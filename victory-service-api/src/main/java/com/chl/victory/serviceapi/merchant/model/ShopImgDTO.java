package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopImgDTO extends BaseDTO implements Serializable {
    /**
     * 图片标识，zimg则为md5
     * img_id
     */
    private String imgId;

    /**
     * 图片大小，单位byte
     * size
     */
    private Long size;

    /**
     * 引用次数
     */
    private Integer usedNum;

    public void setImgId(String imgId) {
        this.imgId = imgId == null ? null : imgId.trim();
    }

}