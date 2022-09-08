package com.chl.victory.dao.query.item;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemQuery extends BaseQuery {
    /**
     * 归属叶子类目ID
     * leaf_cate_id
     */
    private Long leafCateId;

    private List<Long> leafCateIds;

    /**
     * 外部商品编码
     * outer_no
     */
    private String outerNo;

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
     * 一口价
     * price
     */
    private BigDecimal price;

    /**
     * 状态，10:已发布/仓库中;20:准备上架;30:已上架;40:售罄;50:预警
     * status
     */
    private Byte status;
    private Byte statusStart;

    private Integer presell;
    private boolean needFillCacheData;
}