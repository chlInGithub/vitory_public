package com.chl.victory.web.model;

import lombok.Data;

/**
 * dataTables js组件，分页列表接口通用返回值
 * <br/>
 * draw recordsFiltered recordsFiltered 必填，否则table使用错误
 *
 * @author hailongchen9
 */
@Data
public class PageResult<T>{
    /**
     * base 0
     */
    public Integer pageIndex;
    public Integer pageSize;
    public Integer draw;
    public Integer recordsTotal;
    public Integer recordsFiltered;
    public String error;
    // 业务处理是否成功
    public boolean s;
    public T data;

    public static <T> PageResult SUCCESS(T data, Integer draw, Integer pageIndex, Integer total){
        PageResult result = new PageResult();
        result.setDraw(draw);
        result.setPageIndex(pageIndex);
        result.setRecordsTotal(total);
        result.setRecordsFiltered(total);
        result.setData(data);
        result.setS(true);
        return result;
    }
    public static PageResult FAIL(String msg,Integer code, Integer draw, Integer pageIndex, Integer total){
        PageResult result = new PageResult();
        result.setError(code + "|" + msg);
        result.setDraw(draw);
        result.setPageIndex(pageIndex);
        result.setRecordsTotal(total);
        result.setS(false);
        return result;
    }
}
