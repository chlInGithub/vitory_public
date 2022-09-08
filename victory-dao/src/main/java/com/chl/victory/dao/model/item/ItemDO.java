package com.chl.victory.dao.model.item;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author hailongchen9
 */
@Data
public class ItemDO extends BaseDO {
    /**
     * 归属叶子类目ID
     * leaf_cate_id
     */
    @NotNull
    private Long leafCateId;

    /**
     * 外部商品编码
     * outer_no
     */
    private String outerNo;

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
     * 关键字，分隔符逗号
     * key
     */
    private String key;

    /**
     * 成本
     * cost
     */
    private BigDecimal cost;

    /**
     * 吊牌价
     * tag_price
     */
    @NotNull
    private BigDecimal tagPrice;

    /**
     * 一口价
     * price
     */
    @NotNull
    @Positive
    private BigDecimal price;

    /**
     * 库存总量，单位件
     * inventory
     */
    private Integer inventory;

    /**
     * 起卖数
     * minimum
     */
    // private Integer minimum;

    /**
     * 销量
     * sales
     */
    private Integer sales;
    private Integer saleUV;
    private List<String> saleUsers;

    /**
     * 属性，如重量、体积等,json格式
     * attr
     * @see ItemAttributeDO
     */
    private String attr;

    /**
     * 图片，分隔符逗号
     * imgs
     */
    @NotEmpty
    private String imgs;

    /**
     * 详情html表示
     * detail_html
     */
    @NotEmpty
    private String detailHtml;

    /**
     * 物流配置,json格式
     * logistics
     * @see ItemLogisticsConfigDO
     */
    @NotEmpty
    private String logistics;

    /**
     * 状态，10:已发布/仓库中;20:准备上架;30:已上架;40:售罄;50:预警
     * status
     */
    private Byte status;

    /**
     * 是否预售商品
     * 0: N；1：Y
     */
    // Integer presell = 0;

    /**
     * 针对预售商品，开始发货日期
     */
    // Date startSent;

    boolean existSku;

    /**
     * 销售策略数据 json
     */
    List<String> saleStrategies;

    //private Long tag;

    public String getFirstImg(){
        if (imgs == null || imgs.trim().length() == 0){
            return "";
        }
        return imgs.trim().split(",")[0];
    }

}