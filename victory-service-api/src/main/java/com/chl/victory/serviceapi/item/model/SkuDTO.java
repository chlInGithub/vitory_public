package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;
import java.math.BigDecimal;
import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class SkuDTO extends BaseDTO implements Serializable {
    /**
     * 归属商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 标题
     * title
     */
    private String title;

    /**
     * 子标题
     * sub_title
     */
    private String subTitle;

    /**
     * 成本
     * cost
     */
    private BigDecimal cost;

    /**
     * 吊牌价
     * tag_price
     */
    private BigDecimal tagPrice;

    /**
     * 一口价
     * price
     */
    private BigDecimal price;

    /**
     * 库存总量，单位件
     * inventory
     */
    private Integer inventory;

    /**
     * 销量
     * sales
     */
    private Integer sales;

    /**
     * 图片，分隔符逗号
     * imgs
     */
    private String imgs;


    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle == null ? null : subTitle.trim();
    }

    public void setImgs(String imgs) {
        this.imgs = imgs == null ? null : imgs.trim();
    }

    public String getFirstImg(){
        if (imgs == null || imgs.trim().length() == 0){
            return "";
        }
        return imgs.trim().split(",")[0];
    }
}