package com.chl.victory.dao.query;

import lombok.Data;

/**
 * @author hailongchen9
 */
@Data
public class PageResult<T>{
    public Integer total;
    public T data;

    public PageResult(Integer total, T data) {
        this.total = total;
        this.data = data;
    }
}
