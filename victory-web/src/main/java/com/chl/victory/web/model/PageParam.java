package com.chl.victory.web.model;

import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class PageParam {
    /**
     * 页码，base 0
     */
    Integer pageIndex;
    /**
     * 起始记录数, base 0
     */
    Integer dataStart;
    /**
     * 单页记录数
     */
    Integer pageSize;
    /**
     * 绘制计数，与dataTables js组件有关，验证返回与请求中draw相等
     */
    Integer draw;
}
