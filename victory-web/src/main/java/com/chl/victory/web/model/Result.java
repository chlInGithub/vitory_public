package com.chl.victory.web.model;

import lombok.Data;

/**
 * 接口通用返回值
 *
 * @author hailongchen9
 */
@Data
public class  Result<T> {
    public String m;
    public Integer c;
    // 业务处理是否成功
    public boolean s;
    public T d;

    public static <T> Result SUCCESS(T data){
        Result result = new Result();
        result.setS(true);
        result.setD(data);
        return result;
    }
    public static Result SUCCESS(){
        Result result = new Result();
        result.setS(true);
        return result;
    }
    public static Result FAIL(String msg, Integer code){
        Result result = new Result();
        result.setS(false);
        result.setM(msg);
        result.setC(code);
        return result;
    }
    public static Result FAIL(String msg){
        Result result = new Result();
        result.setS(false);
        result.setM(msg);
        result.setC(-1);
        return result;
    }
    public static Result FAIL(){
        Result result = new Result();
        result.setS(false);
        return result;
    }

}
