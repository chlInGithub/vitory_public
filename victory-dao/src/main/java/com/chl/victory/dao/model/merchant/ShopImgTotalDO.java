package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;
import javax.persistence.*;

@Table(name = "`shop_img_total`")
public class ShopImgTotalDO extends BaseDO {
    /**
     * 图片大小，单位byte
     * size
     */
    private Long size;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}