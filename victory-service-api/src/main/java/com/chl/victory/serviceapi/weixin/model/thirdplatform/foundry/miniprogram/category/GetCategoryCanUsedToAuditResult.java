package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 9:24
 **/
@Data
public class GetCategoryCanUsedToAuditResult extends BaseResult  implements Serializable {
    Category[] category_list;

    @Data
    public static class Category implements Serializable{

        /**
         * 一级类目名称
         */
        String first_class;
        String second_class;
        String third_class;
        Integer first_id;
        Integer second_id;
        Integer third_id;
    }
}
