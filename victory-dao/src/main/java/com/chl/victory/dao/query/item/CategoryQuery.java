package com.chl.victory.dao.query.item;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

@Data
public class CategoryQuery extends BaseQuery {
    /**
     * 类目名称
     * name
     */
    private String name;

    /**
     * 类目级别，默认1级
     * level
     */
    private Byte level;

    /**
     * 上级类目ID
     * parent_id
     */
    private Long parentId;

    private Integer show;
}