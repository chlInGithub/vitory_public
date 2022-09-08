package com.chl.victory.serviceapi.weixin.model.thirdplatform.component;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 9:55
 **/
@Data
public class GetTemplatesResult extends BaseResult implements Serializable {
    Template[] template_list;

    @Data
    public static class Template implements Serializable{
        Long create_time;

        /**
         * 模版版本号，开发者自定义字段
         */
        String user_version;

        /**
         * 模版描述，开发者自定义字段
         */
        String user_desc;

        /**
         * 模版 id
         */
        String template_id;
    }
}
