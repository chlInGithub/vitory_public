package com.chl.victory.dao.model.item;

import com.chl.victory.dao.model.BaseDO;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class SkuDO extends BaseDO {
    /**
     * 归属商品ID
     * item_id
     */
    private Long itemId;

    /**
     * 标题
     * title
     */
    @NotEmpty
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
    @NotNull @Positive
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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle == null ? null : subTitle.trim();
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getTagPrice() {
        return tagPrice;
    }

    public void setTagPrice(BigDecimal tagPrice) {
        this.tagPrice = tagPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgs() {
        return imgs;
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