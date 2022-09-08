package com.chl.victory.dao.model.merchant;

import com.chl.victory.dao.model.BaseDO;
import javax.persistence.*;

@Table(name = "`shop_img`")
public class ShopImgDO extends BaseDO {
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

    public Integer getUsedNum() {
        return usedNum;
    }

    public void setUsedNum(Integer usedNum) {
        this.usedNum = usedNum;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId == null ? null : imgId.trim();
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}