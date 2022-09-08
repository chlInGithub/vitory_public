package com.chl.victory.wmall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
@ApiModel(value = "分页查询参数")
public class PageParam {
    /**
     * 页码，base 0
     */
    @ApiModelProperty(value = "页码，base 0")
    Integer pageIndex;
    /**
     * 起始记录数, base 0
     */
    @ApiModelProperty(value = "起始记录数, base 0")
    Integer dataStart;
    /**
     * 单页记录数
     */
    @ApiModelProperty(value = "单页记录数")
    Integer pageSize;
    /**
     * 绘制计数，与dataTables js组件有关，验证返回与请求中draw相等
     */
    Integer draw;
}
