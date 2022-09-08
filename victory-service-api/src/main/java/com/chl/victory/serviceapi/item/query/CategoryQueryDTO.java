package com.chl.victory.serviceapi.item.query;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

@Data
public class CategoryQueryDTO extends BaseQuery implements Serializable {
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