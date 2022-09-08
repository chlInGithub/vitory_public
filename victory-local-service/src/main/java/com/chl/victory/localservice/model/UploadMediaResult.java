package com.chl.victory.localservice.model;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 上传素材结果
 * @author ChenHailong
 * @date 2020/5/28 9:55
 **/
@Data
public class UploadMediaResult implements Serializable {
    String type;
    String media_id;
    String created_at;

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
