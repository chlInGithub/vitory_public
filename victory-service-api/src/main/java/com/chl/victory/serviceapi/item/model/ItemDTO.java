package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class ItemDTO extends BaseDTO implements Serializable {
    /**
     * 归属叶子类目ID
     * leaf_cate_id
     */
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
     * @see ItemAttributeDTO
     */
    private String attr;

    /**
     * 图片，分隔符逗号
     * imgs
     */
    private String imgs;

    /**
     * 详情html表示
     * detail_html
     */
    private String detailHtml;

    /**
     * 物流配置,json格式
     * logistics
     * @see ItemLogisticsConfigDTO
     */
    private String logistics;

    /**
     * 状态，10:已发布/仓库中;20:准备上架;30:已上架;40:售罄;50:预警
     * status
     * @see com.chl.victory.serviceapi.item.enums.ItemStatusEnum
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
     * 销售策略数据, json
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