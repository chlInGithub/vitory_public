package com.chl.victory.serviceapi;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class BaseQuery extends BaseDTO  implements Serializable {
    List<Long> ids;
    Date startedCreatedTime;
    Date endedCreatedTime;
    Date startedModifiedTime;
    Date endedModifiedTime;
    /**
     * 页码，base 0
     */
    Integer pageIndex;
    /**
     * 单页记录数
     */
    Integer pageSize;
    Integer rowStart;

    /**
     * 单列排序优先
     */
    String orderColumn;
    /**
     * 默认倒序
     */
    boolean desc = true;

    /**
     * 仅查询部分信息
     */
    boolean justOutline;

    /**
     * 支持多列排序，但单列排序指定列时优先
     */
    List<OrderedColumn> orderedColumns;

    public Integer getRowStart() {
        if (null != pageIndex && null != pageSize){
            return pageIndex * pageSize;
        }
        return null;
    }

    @Data
    public static class OrderedColumn{
        String orderColumn;
        boolean desc = true;
    }
}
