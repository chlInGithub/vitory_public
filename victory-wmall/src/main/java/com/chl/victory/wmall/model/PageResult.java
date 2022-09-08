package com.chl.victory.wmall.model;

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
    public Integer c;
    public String m;
    // 业务处理是否成功
    public boolean s;
    public T d;

    public static <T> PageResult SUCCESS(T data, Integer draw, Integer pageIndex, Integer total){
        PageResult result = new PageResult();
        result.setDraw(draw);
        result.setPageIndex(pageIndex);
        result.setC(total);
        result.setD(data);
        result.setS(true);
        return result;
    }
    public static PageResult FAIL(String msg,Integer code, Integer draw, Integer pageIndex, Integer total){
        PageResult result = new PageResult();
        result.setM(code + "|" + msg);
        result.setDraw(draw);
        result.setPageIndex(pageIndex);
        result.setC(total);
        result.setS(false);
        return result;
    }
}
