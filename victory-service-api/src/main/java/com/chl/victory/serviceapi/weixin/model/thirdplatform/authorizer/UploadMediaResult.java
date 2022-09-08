package com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * 上传素材结果
 * @author ChenHailong
 * @date 2020/5/28 9:55
 **/
@Data
public class UploadMediaResult extends BaseResult implements Serializable {
    String type;
    String media_id;
    String created_at;
}
