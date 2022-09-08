package com.chl.victory.dao.model.item;

import com.chl.victory.dao.model.BaseDO;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CategoryDO extends BaseDO {
    /**
     * 类目名称
     * name
     */
    @NotNull
    private String name;

    /**
     * 类目级别，默认1级
     * level
     */
    @javax.validation.constraints.Max(3)
    @javax.validation.constraints.Min(1)
    @NotNull
    private Byte level;

    /**
     * 上级类目ID
     * parent_id
     */
    private Long parentId;

    private String img;

    private Integer show;

}