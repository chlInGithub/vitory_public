package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 16:57
 **/
@Data
public class BaseResult  implements Serializable {
    @JSONField(serialize = false)
    Integer errcode = 0;
    @JSONField(serialize = false)
    String errmsg;

    @JSONField(serialize = false)
    public boolean isSuccess(){
        return errcode == null || Integer.valueOf(0).equals(errcode);
    }

    public String getError(){
        return errcode+"|"+errmsg;
    }
}
