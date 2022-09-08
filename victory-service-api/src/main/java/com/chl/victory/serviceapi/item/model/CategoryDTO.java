package com.chl.victory.serviceapi.item.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class CategoryDTO extends BaseDTO implements Serializable {
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

    private String img;

    private Integer show = 1;

}